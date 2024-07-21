package com.example.product.consumer.command;


import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.enumeration.EventResult;
import com.example.product.config.ProductEventQueues;
import com.example.product.config.ProductNotificationProperties;
import com.example.product.dto.command.UpdateProduct;
import com.example.product.enumeration.NotificationType;
import com.example.product.enumeration.ProductEventType;
import com.example.product.exception.ProductException;
import com.example.product.service.ProductNotificationService;
import com.example.product.service.ProductService;
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
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.eventQueues = eventQueues;
        this.notificationProperties = notificationProperties;
    }

    @SqsListener("${sqs-queues.product.commands.update}")
    public void onUpdateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        UpdateProduct eventBody = null;
        Command<UpdateProduct> event = null;
        try {
            event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);
            eventBody = event.getBody();

            productService.updateProduct(eventBody);

            var notificationBody = notificationProperties.getUpdateSuccess();
            notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
            notificationBody.formatTittle(eventBody.id().toString());
            sendNotification(notificationBody, traceId);

            sendEvent(event, null, EventResult.SUCCESS);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", getEventType(), traceId);
            if (e.isRetryable()) {
                throw e;
            }

            var notificationBody = notificationProperties.getUpdateFailed();
            notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
            notificationBody.formatTittle(eventBody.name());
            notificationBody.formatMessage(eventBody.name(), e.getReason());
            sendNotification(notificationBody, traceId);

            assert event != null;
            sendEvent(event, null, EventResult.SUCCESS);
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
