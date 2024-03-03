package com.example.demochild2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String hello(){
        return "Hello";
    }
}
