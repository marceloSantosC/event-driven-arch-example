package com.example.service;

import com.example.dto.command.UserNotification;
import com.example.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;

import static com.example.enumeration.NotificationType.WINDOWS_SYSTEM_TRAY;

@Slf4j
@Service
public class WindowsSystemTrayNotificationService implements NotificationService {

    public void send(UserNotification notifyUser) {
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();

                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image, "Tray icon");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("System tray icon");
                tray.add(trayIcon);

                trayIcon.displayMessage(notifyUser.title(), notifyUser.message(), TrayIcon.MessageType.INFO);
            } else {
                log.error("Failed to send system tray notification: System tray not supported!");
                throw new NotificationException("System tray is not supported.");
            }
        } catch (AWTException e) {
            log.error("Failed to send system tray notification: {}! {}.", e.getClass(), e.getMessage());
            throw new NotificationException(e.getMessage(), e);
        }
    }

    @Override
    public boolean canSendNotification(UserNotification notifyUser) {
        return WINDOWS_SYSTEM_TRAY.equals(notifyUser.type());
    }

}
