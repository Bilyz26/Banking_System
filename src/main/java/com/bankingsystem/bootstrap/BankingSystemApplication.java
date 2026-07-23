package com.bankingsystem.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bankingsystem")
public class BankingSystemApplication {

    private BankingSystemApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }
}

