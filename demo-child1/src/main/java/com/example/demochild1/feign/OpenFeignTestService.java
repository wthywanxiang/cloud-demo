package com.example.demochild1.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("demo-child2")
public interface OpenFeignTestService {
    @RequestMapping("/hello")
    String hello();
}
