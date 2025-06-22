package com.kompetencyjny.EventBuddySpring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventBuddySpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventBuddySpringApplication.class, args);
	}

}
