package com.events.app.myevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyEventsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyEventsApplication.class, args);
	}

}
