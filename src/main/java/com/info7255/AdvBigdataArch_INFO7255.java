package com.info7255;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.info7255")
public class AdvBigdataArch_INFO7255 {

	public static void main(String[] args) {
		SpringApplication.run(AdvBigdataArch_INFO7255.class, args);
	}
}
