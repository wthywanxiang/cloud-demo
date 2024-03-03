package com.example.demochild2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler){
        //浏览器预请求全是OPTIONS方法，如果是浏览器预请求，直接return true;
        if(request.getMethod().equals("OPTIONS")){
            return true;
        }
//        if(request.getHeader("Authorization")==null){
//            System.out.println("request.getHeader(\"Authorization\") = " + request.getHeader("Authorization"));
//            return false;
//        }
        return true;
    }
}