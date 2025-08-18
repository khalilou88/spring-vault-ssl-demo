package com.demo.servera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.demo.servera", "com.demo.common", "com.gridatek"})
public class ServerAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerAApplication.class, args);
    }
}
