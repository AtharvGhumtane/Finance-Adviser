package com.alex.auth_serviceAlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class AuthServiceAlexApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceAlexApplication.class, args);
	}

}
