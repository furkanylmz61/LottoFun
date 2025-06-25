package com.assesment.lottofun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LottofunApplication {

    public static void main(String[] args) {
        SpringApplication.run(LottofunApplication.class, args);
    }

}
