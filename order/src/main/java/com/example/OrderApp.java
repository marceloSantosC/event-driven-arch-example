package com.example;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class OrderApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}