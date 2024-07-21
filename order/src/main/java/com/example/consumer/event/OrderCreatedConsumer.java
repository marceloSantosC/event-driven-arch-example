package com.example.consumer.event;

import com.example.client.SQSClient;
import com.example.config.OrderEventQueues;
import com.example.dto.event.Event;
import com.example.dto.event.OrderCreated;
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
public class OrderCreatedConsumer extends EventConsumer<OrderCreated> {

    private final OrderService orderService;

    private final ObjectMapper objectMapper;

    private final OrderEventQueues eventQueues;

    public OrderCreatedConsumer(SQSClient sqsClient,
                                OrderService orderService,
                                ObjectMapper objectMapper,
                                OrderEventQueues eventQueues) {
        super(sqsClient);
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        this.eventQueues = eventQueues;
    }

    @SqsListener("${sqs-queues.order.events.created}")
    public void onCreateOrder(String message, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        try {
            Event<OrderCreated, OrderEventType> event = objectMapper.readValue(message, new TypeReference<>() {
            });
            orderService.requestShipping(event.getBody());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event with trace id {}.", traceId);
        }
    }

    @Override
    protected OrderEventType generatedEventType() {
        return OrderEventType.RECEIVED;
    }

    @Override
    protected String generatedEventQueue() {
        return eventQueues.getCreated();
    }
}
