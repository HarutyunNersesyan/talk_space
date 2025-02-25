package com.talk_space;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class TalkSpaceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TalkSpaceApplication.class, args);
	}

}
