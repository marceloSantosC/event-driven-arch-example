package com.example.dto.command;

import com.example.enumeration.NotificationType;

public record UserNotification(String title,
                               String message,
                               NotificationType type
) {

}




