package com.example;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class NotificationApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(NotificationApp.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}