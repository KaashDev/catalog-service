package com.nhcarrigan.catalogservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/health")
public class HealthController{

    @GetMapping
    public Map<String, String> healthCheck(){
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        return health;
    }
}


