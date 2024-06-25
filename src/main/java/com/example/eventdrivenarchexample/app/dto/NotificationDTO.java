package com.example.eventdrivenarchexample.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.MessageFormat;

@Setter
@Getter
@NoArgsConstructor
public class NotificationDTO {

    private String title;

    private String message;

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
