package com.example.eventdrivenarchexample;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class EventDrivenArchExampleApplication {


    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EventDrivenArchExampleApplication.class);
        builder.headless(false);
        builder.run(args);
    }
}
