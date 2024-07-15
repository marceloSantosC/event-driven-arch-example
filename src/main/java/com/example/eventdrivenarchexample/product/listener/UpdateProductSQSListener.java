package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import com.example.eventdrivenarchexample.product.dto.command.UpdateProduct;
import com.example.eventdrivenarchexample.product.dto.event.UpdatedProductEvent;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.exception.ProductException;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class UpdateProductSQSListener extends EventListener {

    private final SQSClient sqsClient;

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductNotificationProperties notificationProperties;

    public UpdateProductSQSListener(ProductNotificationService notificationService,
                                    SQSClient sqsClient,
                                    ProductService productService,
                                    ObjectMapper objectMapper,
                                    ProductNotificationProperties notificationProperties) {
        super(notificationService, sqsClient);
        this.sqsClient = sqsClient;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.notificationProperties = notificationProperties;
    }

    @SqsListener(queueNames = {"${event-queues.product.update-events}"})
    public void onUpdateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        UpdateProduct eventBody = null;
        CommandPayload<UpdateProduct> event = null;
        try {
            event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);
            eventBody = event.getBody();

            productService.updateProduct(eventBody);

            var notification = NotifyProductNotification.valueOf(notificationProperties.getUpdateSuccess());
            notification.formatTittle(eventBody.id().toString());
            sendNotification(notification, traceId);

            tryToSendEventResultToCallbackQueue(event, traceId, ProductEventResult.SUCCESS);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", getEventType(), traceId);
            if (e.isRetryable()) {
                throw e;
            }

            var notificationBody = NotifyProductNotification.valueOf(notificationProperties.getUpdateFailed());
            notificationBody.formatTittle(eventBody.name());
            notificationBody.formatMessage(eventBody.name(), e.getReason());
            sendNotification(notificationBody, traceId);

            tryToSendEventResultToCallbackQueue(event, traceId, ProductEventResult.FAIL);
        }

    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.UPDATE;
    }

    private void tryToSendEventResultToCallbackQueue(CommandPayload<UpdateProduct> event, String traceId, ProductEventResult result) {

        if (StringUtils.isBlank(event.getCallbackQueue())) {
            return;
        }

        var eventBody = event.getBody();
        var callbackPayload = UpdatedProductEvent.builder()
                .eventId(eventBody.eventId())
                .productId(eventBody.id())
                .eventType(getEventType())
                .eventResult(result)
                .build();

        sqsClient.sendToSQS(event.getCallbackQueue(), callbackPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));
    }
}
