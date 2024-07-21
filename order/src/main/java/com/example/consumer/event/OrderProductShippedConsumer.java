package com.example.consumer.event;

import com.example.client.SQSClient;
import com.example.config.OrderCommandQueues;
import com.example.config.OrderNotificationProperties;
import com.example.dto.NotificationProperty;
import com.example.dto.command.CancelOrder;
import com.example.dto.command.Command;
import com.example.dto.command.UserNotification;
import com.example.dto.event.Event;
import com.example.dto.event.ShippedProducts;
import com.example.enumeration.EventResult;
import com.example.enumeration.NotificationType;
import com.example.enumeration.ProductEventType;
import com.example.service.NotificationService;
import com.example.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class OrderProductShippedConsumer {

    private final SQSClient sqsClient;

    private final ObjectMapper objectMapper;

    private final OrderService orderService;

    private final OrderCommandQueues orderCommandQueues;

    private final NotificationService notificationService;

    private final OrderNotificationProperties orderNotificationProperties;

    @SqsListener("${sqs-queues.product.events.shipped}")
    public void onOrderProductShipped(String message, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        try {
            Event<ShippedProducts, ProductEventType> event = objectMapper.readValue(message, new TypeReference<>() {
            });
            orderService.updateOrderProductStatus(event);

            if (EventResult.FAIL.equals(event.getEventResult())) {
                Command<CancelOrder> cancelOrderCommand = Command.<CancelOrder>builder()
                        .customId(event.getCustomId())
                        .body(new CancelOrder("Shipping failed: Order has invalid or out of stock products."))
                        .build();
                sqsClient.sendToSQS(orderCommandQueues.getCancel(), cancelOrderCommand, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));
            }

            sendNotification(event, traceId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        }
    }

    private void sendNotification(Event<ShippedProducts, ProductEventType> event, String traceId) {
        NotificationProperty notificationProperty = EventResult.FAIL.equals(event.getEventResult()) ? orderNotificationProperties.getOrderProductsInvalid() :
                orderNotificationProperties.getOrderProductsShipped();
        UserNotification notification = UserNotification.valueOf(notificationProperty, NotificationType.WINDOWS_SYSTEM_TRAY);
        notification.formatMessage(event.getCustomId());
        notificationService.send(notification, traceId);
    }

}
