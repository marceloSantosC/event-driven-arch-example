package com.example.eventdrivenarchexample.product.config;

import com.example.eventdrivenarchexample.app.dto.NotificationDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "push-notifications.product")
public class ProductNotificationProperties {

    private NotificationDTO creationFailed;

    private NotificationDTO creationSuccess;

    private NotificationDTO updateSuccess;

    private NotificationDTO updateFailed;

    private NotificationDTO shipSuccess;

    private NotificationDTO shipFailed;

}
