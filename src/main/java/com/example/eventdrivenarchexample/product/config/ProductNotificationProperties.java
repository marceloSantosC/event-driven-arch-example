package com.example.eventdrivenarchexample.product.config;

import com.example.eventdrivenarchexample.app.dto.PushNotificationDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "push-notifications.product")
public class ProductNotificationProperties {


    @Value("${push-notifications.product.on-creation-fail}")
    private PushNotificationDTO creationFailPushNotification;

    @Value("${push-notifications.product.on-creation-success}")
    private PushNotificationDTO creationSuccessPushNotification;

}
