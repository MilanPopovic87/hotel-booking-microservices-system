package com.hotel.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiChatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiChatServiceApplication.class, args);
	}

}
