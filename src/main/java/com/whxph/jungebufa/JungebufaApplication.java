package com.whxph.jungebufa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author liujun
 */
@EnableScheduling
@SpringBootApplication
public class JungebufaApplication {

	public static void main(String[] args) {
		SpringApplication.run(JungebufaApplication.class, args);
	}
}
