package com.example.eventdrivenarchexample.order.listener;

import com.example.eventdrivenarchexample.order.dto.events.NewOrderEventPayload;
import com.example.eventdrivenarchexample.order.service.OrderService;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewOrderSQSListener {

    private final OrderService orderService;

    private final ObjectMapper objectMapper;
    
    public void onNewOrderEvent(String message) {
        try {
            var newOrderEvent = objectMapper.readValue(message, NewOrderEventPayload.class);
            orderService.createOrder(newOrderEvent);
        } catch (JacksonException e) {
            log.error("Failed to serialize message.");
        }
    }

}
