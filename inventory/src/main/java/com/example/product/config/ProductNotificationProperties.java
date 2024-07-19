package com.example.product.config;

import com.example.product.dto.command.UserNotification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "notification")
public class ProductNotificationProperties {

    private UserNotification creationFailed;

    private UserNotification creationSuccess;

    private UserNotification updateSuccess;

    private UserNotification updateFailed;

    private UserNotification shipSuccess;

    private UserNotification shipFailed;

}
