package pl.gedlek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrafficApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrafficApplication.class, args);
    }
}