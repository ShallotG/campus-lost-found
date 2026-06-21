package com.campus.lostfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CampusLostFoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLostFoundApplication.class, args);
    }
}
