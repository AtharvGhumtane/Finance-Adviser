package com.alex.ai_serviceAlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;


@EnableDiscoveryClient
@EnableR2dbcRepositories
@SpringBootApplication
public class AiServiceAlexApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiServiceAlexApplication.class, args);
	}

}
