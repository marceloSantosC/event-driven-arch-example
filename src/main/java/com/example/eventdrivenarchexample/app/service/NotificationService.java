package com.example.eventdrivenarchexample.app.service;

import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;

public interface NotificationService {

    void send(NotificationBodyInput notificationBodyDTO);

}
