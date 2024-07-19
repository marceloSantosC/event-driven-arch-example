package com.example.service;

import com.example.dto.command.UserNotification;

public interface NotificationService {

    void send(UserNotification notifyUser);

    boolean canSendNotification(UserNotification notifyUser);

}
