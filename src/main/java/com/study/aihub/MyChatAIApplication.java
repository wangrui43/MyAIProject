package com.study.aihub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.study.aihub.mapper")
@SpringBootApplication
public class MyChatAIApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyChatAIApplication.class, args);
	}

}
