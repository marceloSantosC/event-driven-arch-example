package com.example.product.dto.command;

import com.example.product.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.MessageFormat;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

    private String title;

    private String message;

    private NotificationType type;

    public void formatTittle(String... vars) {
        title = setVariables(title, vars);
    }

    public void formatMessage(String... vars) {
        message = setVariables(message, vars);
    }

    private String setVariables(String template, String... vars) {
        MessageFormat messageFormat = new MessageFormat(template);
        return messageFormat.format(vars);
    }

}
