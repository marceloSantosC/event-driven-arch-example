package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.input.EventPayload;
import com.example.eventdrivenarchexample.product.dto.input.NewProductInput;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
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
public class CreateProductSQSListener extends EventListenerWithNotification {

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductNotificationProperties notificationProperties;

    public CreateProductSQSListener(ObjectMapper objectMapper, ProductService productService,
                                    ProductNotificationService notificationService,
                                    ProductNotificationProperties notificationProperties) {
        super(notificationService);
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.notificationProperties = notificationProperties;
    }


    @SqsListener(queueNames = {"${event-queues.product.create-events}"})
    public void onCreateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);

        NewProductInput newProduct = null;
        try {
            EventPayload<NewProductInput> event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            newProduct = event.getBody();

            var id = productService.createProduct(event.getBody());

            var notificationBody = NotificationBodyInput.valueOf(notificationProperties.getCreationSuccess());
            notificationBody.formatTittle(newProduct.name());
            notificationBody.formatMessage(newProduct.name(), id.toString());
            sendNotification(notificationBody, traceId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", getEventType(), traceId);

            if (e.isRetryable()) {
                throw e;
            }

            var notificationBody = NotificationBodyInput.valueOf(notificationProperties.getCreationFailed());
            notificationBody.formatTittle(newProduct.name());
            notificationBody.formatMessage(newProduct.name(), e.getReason());
            sendNotification(notificationBody, traceId);
        }
    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.CREATION;
    }

}
