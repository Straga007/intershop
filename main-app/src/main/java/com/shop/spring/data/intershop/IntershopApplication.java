package com.shop.spring.data.intershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class IntershopApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntershopApplication.class, args);
	}

}