package dev.midnightcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MidnightTechnician {

    static void main(String[] a) {
        SpringApplication.run(MidnightTechnician.class);
    }
}
