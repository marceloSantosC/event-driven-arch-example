package com.example.eventdrivenarchexample.product.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "event-queues.product")
public class ProductEventQueueProperties {

    @Value("${event-queues.product.create-events}")
    private String createEventsQueue;

    @Value("${event-queues.product.notification-events}")
    private String productNotificationEventsQueue;
    
}
