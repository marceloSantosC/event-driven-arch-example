package com.example.consumer.command;

import com.example.client.SQSClient;
import com.example.config.OrderNotificationProperties;
import com.example.dto.NotificationProperty;
import com.example.dto.command.CancelOrder;
import com.example.dto.command.Command;
import com.example.dto.command.UserNotification;
import com.example.enumeration.NotificationType;
import com.example.exception.OrderException;
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
public class CancelOrderCommand {

    private final OrderService orderService;

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final OrderNotificationProperties orderNotificationProperties;

    @SqsListener("${sqs-queues.order.commands.cancel}")
    public void cancelOrder(String message, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        Command<CancelOrder> command = null;
        try {
            command = objectMapper.readValue(message, new TypeReference<>() {
            });
            orderService.cancelOrder(Long.valueOf(command.getCustomId()), command.getBody());

            NotificationProperty notificationProperty = orderNotificationProperties.getOrderCancelSuccess();
            UserNotification notification = UserNotification.valueOf(notificationProperty, NotificationType.WINDOWS_SYSTEM_TRAY);
            notification.formatTitle(command.getCustomId());
            notificationService.send(notification, traceId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        } catch (OrderException e) {
            log.error("Failed to cancel order {}.", e.getMessage());
            NotificationProperty notificationProperty = orderNotificationProperties.getOrderCancelFail();
            UserNotification notification = UserNotification.valueOf(notificationProperty, NotificationType.WINDOWS_SYSTEM_TRAY);
            notification.formatMessage(e.getMessage());
            notification.formatTitle(command.getCustomId());
            notificationService.send(notification, traceId);
        }

    }

}
