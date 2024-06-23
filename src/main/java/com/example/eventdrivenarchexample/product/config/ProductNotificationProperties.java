package com.example.eventdrivenarchexample.product.config;

import com.example.eventdrivenarchexample.app.dto.PushNotificationDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "push-notifications.product")
public class ProductNotificationProperties {

    private PushNotificationDTO creationFailed;

    private PushNotificationDTO creationSuccess;

}
