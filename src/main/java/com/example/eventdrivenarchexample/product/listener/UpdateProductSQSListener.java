package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.UpdateProductInput;
import com.example.eventdrivenarchexample.product.dto.output.UpdatedProductEvent;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.exception.ProductException;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class UpdateProductSQSListener extends EventListenerWithNotification {

    private final SQSClient sqsClient;

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductNotificationProperties notificationProperties;

    public UpdateProductSQSListener(ProductNotificationService notificationService, SQSClient sqsClient, ProductService productService,
                                    ObjectMapper objectMapper, ProductNotificationProperties notificationProperties) {
        super(notificationService);
        this.sqsClient = sqsClient;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.notificationProperties = notificationProperties;
    }

    @SqsListener(queueNames = {"${event-queues.product.update-events}"})
    public void onCreateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        UpdateProductInput event = null;
        try {
            event = objectMapper.readValue(payload, UpdateProductInput.class);
            productService.updateProduct(event);

            var notification = NotificationBodyInput.valueOf(notificationProperties.getUpdateSuccess());
            notification.formatTittle(event.id().toString());
            sendNotification(notification, traceId);

            if (StringUtils.isNotBlank(event.callbackQueue())) {
                sendEventResultToCallbackQueue(event, traceId, ProductEventResult.SUCCESS);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", getEventType(), traceId);
            if (e.isRetryable()) {
                throw e;
            }

            var notificationBody = NotificationBodyInput.valueOf(notificationProperties.getUpdateFailed());
            notificationBody.formatTittle(event.name());
            notificationBody.formatMessage(event.name(), e.getReason());
            sendNotification(notificationBody, traceId);
            sendEventResultToCallbackQueue(event, traceId, ProductEventResult.FAIL);
        }

    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.UPDATE;
    }

    private void sendEventResultToCallbackQueue(UpdateProductInput event, String traceId, ProductEventResult result) {
        var callbackPayload = UpdatedProductEvent.builder()
                .eventId(event.eventId())
                .productId(event.id())
                .eventType(getEventType())
                .eventResult(result)
                .build();

        sqsClient.sendToSQS(event.callbackQueue(), callbackPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));
    }
}
