package com.example.eventdrivenarchexample.product.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sqs-queues.product.events")
public class ProductEventQueues {

    private String created;

    private String updated;

    private String shipped;

}
