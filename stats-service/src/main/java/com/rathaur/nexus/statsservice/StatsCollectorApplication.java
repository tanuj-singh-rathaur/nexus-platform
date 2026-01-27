package com.rathaur.nexus.statsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatsCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatsCollectorApplication.class, args);
	}

}
