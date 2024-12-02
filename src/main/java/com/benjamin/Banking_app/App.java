package com.benjamin.Banking_app;

import com.benjamin.Banking_app.Security.AuthenticationService;
import com.benjamin.Banking_app.Security.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.benjamin.Banking_app.Roles.Role.ADMIN;
import static com.benjamin.Banking_app.Roles.Role.USER;

@SpringBootApplication
@ComponentScan(basePackages = "com.benjamin.Banking_app")
@EnableScheduling
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner( //to inject beans from our app. for application startup
				AuthenticationService service
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstName("Admin")
					.lastName("Admin")
					.email("admin@mail.com")
					.password("password")
					.role(ADMIN)
					.build();

			var user = RegisterRequest.builder()
					.firstName("User")
					.lastName("User")
					.email("user@gmail.com")
					.password("password")
					.role(USER)
					.build();

			System.out.println("Admin token is: " + service.register(admin).getToken());
			System.out.println("User token is: " + service.register(user).getToken());
		};
	}
}