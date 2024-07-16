package com.example.eventdrivenarchexample.product.consumer.command;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductEventQueues;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import com.example.eventdrivenarchexample.product.dto.command.UpdateProduct;
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
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class UpdateProductConsumer extends CommandConsumer<Void> {

    private final SQSClient sqsClient;

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductEventQueues eventQueues;

    private final ProductNotificationProperties notificationProperties;


    public UpdateProductConsumer(ProductNotificationService notificationService,
                                 SQSClient sqsClient,
                                 ProductService productService,
                                 ObjectMapper objectMapper,
                                 ProductEventQueues eventQueues,
                                 ProductNotificationProperties notificationProperties) {
        super(notificationService, sqsClient);
        this.sqsClient = sqsClient;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.eventQueues = eventQueues;
        this.notificationProperties = notificationProperties;
    }

    @SqsListener("${sqs-queues.product.commands.update}")
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

            sendEvent(event, null, ProductEventResult.SUCCESS);

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

            assert event != null;
            sendEvent(event, null, ProductEventResult.SUCCESS);
        }

    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.UPDATE;
    }

    @Override
    protected String getEventQueue() {
        return eventQueues.getUpdated();
    }

}
