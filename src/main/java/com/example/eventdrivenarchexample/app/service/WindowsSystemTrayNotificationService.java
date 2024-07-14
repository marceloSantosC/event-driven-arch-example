package com.example.eventdrivenarchexample.app.service;

import com.example.eventdrivenarchexample.app.exception.NotificationException;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;

@Slf4j
@Service
public class WindowsSystemTrayNotificationService implements NotificationService {

    public void send(NotificationBodyInput notificationBodyDTO) {
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();

                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image, "Tray icon");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("System tray icon");
                tray.add(trayIcon);

                trayIcon.displayMessage(notificationBodyDTO.getTitle(), notificationBodyDTO.getMessage(), TrayIcon.MessageType.INFO);
            } else {
                log.error("Failed to send system tray notification: System tray not supported!");
                throw new NotificationException("System tray is not supported.", false);
            }
        } catch (AWTException e) {
            log.error("Failed to send system tray notification: {}! {}.", e.getClass(), e.getMessage());
            throw new NotificationException(e.getMessage(), e, false);
        }
    }

}
