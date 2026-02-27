package com.taxoptimizerAlexz.tax_optimizerAlexz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "com.taxoptimizer.repo")
public class TaxOptimizerAlexzApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxOptimizerAlexzApplication.class, args);
	}

}
