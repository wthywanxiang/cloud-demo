package com.example.demochild1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.demochild1.feign")
public class DemoChild1Application {

    public static void main(String[] args) {
        SpringApplication.run(DemoChild1Application.class, args);
    }

}
