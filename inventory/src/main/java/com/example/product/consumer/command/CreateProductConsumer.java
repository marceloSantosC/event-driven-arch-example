package com.example.product.consumer.command;


import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.enumeration.EventResult;
import com.example.product.config.ProductEventQueues;
import com.example.product.config.ProductNotificationProperties;
import com.example.product.dto.command.CreateProduct;
import com.example.product.dto.event.CreatedProduct;
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
public class CreateProductConsumer extends CommandConsumer<CreatedProduct> {

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductEventQueues eventQueues;

    private final ProductNotificationProperties notificationProperties;

    public CreateProductConsumer(ObjectMapper objectMapper,
                                 ProductService productService,
                                 SQSClient sqsClient,
                                 ProductNotificationService notificationService, ProductEventQueues eventQueues,
                                 ProductNotificationProperties notificationProperties) {
        super(notificationService, sqsClient);
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.eventQueues = eventQueues;
        this.notificationProperties = notificationProperties;
    }


    @SqsListener(queueNames = {"${sqs-queues.product.commands.create}"})
    public void createProduct(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);

        Command<CreateProduct> event = null;
        try {
            event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);

            CreatedProduct createdProduct = productService.createProduct(event.getBody());

            sendEvent(event, createdProduct, EventResult.SUCCESS);

            var notificationBody = notificationProperties.getCreationSuccess();
            notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
            notificationBody.formatTittle(event.getBody().name());
            notificationBody.formatMessage(event.getBody().name(), createdProduct.id().toString());
            sendNotification(notificationBody, traceId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", getEventType(), traceId);

            if (e.isRetryable()) {
                throw e;
            }

            assert event != null;
            super.sendEvent(event, null, EventResult.FAIL);

            var notificationBody = notificationProperties.getCreationFailed();
            notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
            notificationBody.formatTittle(event.getBody().name());
            notificationBody.formatMessage(event.getBody().name(), e.getReason());
            sendNotification(notificationBody, traceId);
        }
    }


    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.CREATION;
    }

    @Override
    protected String getEventQueue() {
        return eventQueues.getCreated();
    }

}
