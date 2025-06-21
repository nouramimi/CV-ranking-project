package com.example.cvfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CvFilterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvFilterApplication.class, args);
	}

}
