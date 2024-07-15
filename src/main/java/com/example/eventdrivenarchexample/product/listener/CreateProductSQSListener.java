package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.CreateProduct;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import com.example.eventdrivenarchexample.product.dto.event.CreatedProduct;
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
public class CreateProductSQSListener extends EventListener<CreatedProduct> {

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductNotificationProperties notificationProperties;

    public CreateProductSQSListener(ObjectMapper objectMapper,
                                    ProductService productService,
                                    SQSClient sqsClient,
                                    ProductNotificationService notificationService,
                                    ProductNotificationProperties notificationProperties) {
        super(notificationService, sqsClient);
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.notificationProperties = notificationProperties;
    }


    @SqsListener(queueNames = {"${event-queues.product.create-events}"})
    public void onCreateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);


        CommandPayload<CreateProduct> event = null;
        try {
            event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);

            CreatedProduct createdProduct = productService.createProduct(event.getBody());
            super.tryToSendEventResultToCallbackQueue(event, createdProduct, ProductEventResult.SUCCESS);

            var notificationBody = NotifyProductNotification.valueOf(notificationProperties.getCreationSuccess());
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

            super.tryToSendEventResultToCallbackQueue(event, null, ProductEventResult.FAIL);

            var notificationBody = NotifyProductNotification.valueOf(notificationProperties.getCreationFailed());
            notificationBody.formatTittle(event.getBody().name());
            notificationBody.formatMessage(event.getBody().name(), e.getReason());
            sendNotification(notificationBody, traceId);
        }
    }


    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.CREATION;
    }

}
