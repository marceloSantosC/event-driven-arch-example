package com.example.dto.command;

import com.example.dto.NotificationProperty;
import com.example.enumeration.NotificationType;
import lombok.*;

import java.text.MessageFormat;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

    private String title;

    private String message;

    private NotificationType type;

    public void formatMessage(String... vars) {
        message = setVariables(message, vars);
    }

    public void formatTitle(String... vars) {
        title = setVariables(title, vars);
    }

    private String setVariables(String template, String... vars) {
        if (template == null) {
            return null;
        }

        MessageFormat messageFormat = new MessageFormat(template);
        return messageFormat.format(vars);
    }

    public static UserNotification valueOf(NotificationProperty property, NotificationType type) {
        return UserNotification.builder()
                .title(property.title())
                .message(property.message())
                .type(type)
                .build();
    }

}
