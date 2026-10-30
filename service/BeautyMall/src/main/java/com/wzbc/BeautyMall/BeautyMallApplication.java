package com.wzbc.BeautyMall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wzbc.BeautyMall.Mapper")
@SpringBootApplication
public class BeautyMallApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeautyMallApplication.class, args);
	}

}
