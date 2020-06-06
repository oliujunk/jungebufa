package com.whxph.jungebufa;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;

/**
 * @author liujun
 */
@EnableScheduling
@SpringBootApplication
public class JungebufaApplication implements ApplicationRunner {

	@Resource
	private Junge junge;

	@Resource
	private Chunkai chunkai;

	public static void main(String[] args) {
		SpringApplication.run(JungebufaApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		junge.start();
		chunkai.start();
	}
}
