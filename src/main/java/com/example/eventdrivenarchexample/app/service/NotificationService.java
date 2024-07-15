package com.example.eventdrivenarchexample.app.service;

import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;

public interface NotificationService {

    void send(NotifyProductNotification notificationBodyDTO);

}
