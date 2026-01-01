package com.alexZ.api_gatewayAlexz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayAlexzApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayAlexzApplication.class, args);
	}

}
