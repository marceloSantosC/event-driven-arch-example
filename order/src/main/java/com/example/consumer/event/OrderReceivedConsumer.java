package com.example.consumer.event;

import com.example.client.SQSClient;
import com.example.config.OrderEventQueues;
import com.example.dto.event.Event;
import com.example.dto.event.OrderCreated;
import com.example.dto.event.OrderReceived;
import com.example.enumeration.EventResult;
import com.example.enumeration.OrderEventType;
import com.example.service.OrderService;
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
public class OrderReceivedConsumer extends EventConsumer<OrderCreated> {

    private final OrderService orderService;

    private final ObjectMapper objectMapper;

    private final OrderEventQueues eventQueues;

    public OrderReceivedConsumer(SQSClient sqsClient,
                                 OrderService orderService,
                                 ObjectMapper objectMapper, OrderEventQueues eventQueues) {
        super(sqsClient);
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        this.eventQueues = eventQueues;
    }

    @SqsListener("${sqs-queues.order.events.received}")
    public void onReceiveOrder(String message, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        try {
            Event<OrderReceived, OrderEventType> event = objectMapper.readValue(message, new TypeReference<>() {
            });
            OrderCreated orderCreated = orderService.createOrder(event.getBody());
            super.sendEvent(event, orderCreated, EventResult.SUCCESS, traceId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}: {}", traceId, e.getMessage(), e);
        }
    }

    @Override
    protected OrderEventType getEventType() {
        return OrderEventType.RECEIVED;
    }

    @Override
    protected String getEventQueue() {
        return eventQueues.getCreated();
    }
}
