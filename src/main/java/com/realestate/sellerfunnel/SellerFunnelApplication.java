package com.realestate.sellerfunnel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SellerFunnelApplication {
    public static void main(String[] args) {
        SpringApplication.run(SellerFunnelApplication.class, args);
    }
}