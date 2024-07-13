package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.events.request.NewProductDTO;
import com.example.eventdrivenarchexample.product.dto.events.request.NotificationBodyDTO;
import com.example.eventdrivenarchexample.product.dto.events.request.ProductNotificationDTO;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.exception.ProductException;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateProductSQSListener {

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final ProductNotificationService notificationService;

    private final ProductNotificationProperties notificationProperties;


    @SqsListener(queueNames = {"${event-queues.product.create-events}"})
    public void onCreateProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        NewProductDTO event = null;
        try {
            event = objectMapper.readValue(payload, NewProductDTO.class);
            var id = productService.createProduct(event);

            var notificationBody = NotificationBodyDTO.valueOf(notificationProperties.getCreationSuccess());
            notificationBody.formatTittle(event.name());
            notificationBody.formatMessage(event.name(), id.toString());
            sendNotification(notificationBody, traceId, ProductEventResult.SUCCESS);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", ProductEventType.CREATION, traceId);
        } catch (ProductException e) {
            log.error("Exception when processing product event : {}. Trace id: {}.", ProductEventType.CREATION, traceId);

            if (e.isRetryable()) {
                throw e;
            }

            var notificationBody = NotificationBodyDTO.valueOf(notificationProperties.getCreationFailed());
            notificationBody.formatTittle(event.name());
            notificationBody.formatMessage(event.name(), e.getReason());
            sendNotification(notificationBody, traceId, ProductEventResult.FAIL);
        }
    }

    private void sendNotification(NotificationBodyDTO notificationBody, String traceId, ProductEventResult result) {
        var notification = ProductNotificationDTO.builder()
                .eventType(ProductEventType.CREATION)
                .result(result)
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

}
