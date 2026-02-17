package com.sijanstu.vendor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.sijanstu.vendor_service",
        "com.sijanstu.ncm_listener"
})
@EnableAsync
@EnableScheduling
public class VendorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorServiceApplication.class, args);
    }
}
