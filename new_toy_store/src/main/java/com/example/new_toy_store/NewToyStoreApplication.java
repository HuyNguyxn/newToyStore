package com.example.new_toy_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NewToyStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewToyStoreApplication.class, args);
	}

}
