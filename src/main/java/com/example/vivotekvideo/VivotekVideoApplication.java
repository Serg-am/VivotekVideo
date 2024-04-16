package com.example.vivotekvideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VivotekVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VivotekVideoApplication.class, args);
    }

}
