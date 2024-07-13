package com.example.eventdrivenarchexample.app.service;

import com.example.eventdrivenarchexample.product.dto.events.request.NotificationBodyDTO;

public interface NotificationService {

    void send(NotificationBodyDTO notificationBodyDTO);

}
