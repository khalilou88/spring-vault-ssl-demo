package com.demo.serverb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.demo.serverb", "com.demo.common", "com.gridatek"})
public class ServerBApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerBApplication.class, args);
    }
}
