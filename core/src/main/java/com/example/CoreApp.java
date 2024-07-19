package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CoreApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CoreApp.class).run(args);
    }

}