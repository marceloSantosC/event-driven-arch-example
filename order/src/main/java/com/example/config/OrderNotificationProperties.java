package com.example.config;


import com.example.dto.NotificationProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification")
public class OrderNotificationProperties {

    private NotificationProperty orderProductsInvalid;

    private NotificationProperty orderProductsShipped;

}
