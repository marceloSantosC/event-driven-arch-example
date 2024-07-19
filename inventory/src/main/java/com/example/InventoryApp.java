package com.example;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class InventoryApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CoreApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}