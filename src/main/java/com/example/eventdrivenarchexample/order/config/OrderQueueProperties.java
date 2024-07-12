package com.example.eventdrivenarchexample.order.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "event-queues.order")
public class OrderQueueProperties {

    @Value("${event-queues.order.received-events}")
    private String receivedOrdersQueue;

    @Value("${event-queues.order.create-events}")
    private String createOrdersQueue;

    @Value("${event-queues.order.failed-events}")
    private String failedOrdersQueue;

}
