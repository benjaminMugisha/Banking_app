package com.benjamin.Banking_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.benjamin.Banking_app")
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
