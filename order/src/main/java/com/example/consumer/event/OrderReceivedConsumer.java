package com.example.consumer.event;

import com.example.client.SQSClient;
import com.example.dto.events.OrderReceived;
import com.example.service.OrderService;
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
public class OrderReceivedConsumer {

    private final OrderService orderService;

    private final ObjectMapper objectMapper;

    @SqsListener("${sqs-queues.order.events.received}")
    public void onReceiveOrder(String message, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        try {
            var newOrderEvent = objectMapper.readValue(message, OrderReceived.class);
            orderService.createOrder(newOrderEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        }
    }

}
