package com.example.tomeettome.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "OK";
    }
}
