package com.example.eventdrivenarchexample.product.dto.input;

import com.example.eventdrivenarchexample.app.dto.NotificationDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.MessageFormat;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBodyInput {

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

    public static NotificationBodyInput valueOf(NotificationDTO notificationDTO) {
        return new NotificationBodyInput(notificationDTO.title(), notificationDTO.message());
    }

}
