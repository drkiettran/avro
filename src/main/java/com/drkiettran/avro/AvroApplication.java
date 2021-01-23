package com.drkiettran.avro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AvroApplication {

	@Autowired
	private AvroHelper avroHelper;
	
	public static void main(String[] args) {
		SpringApplication.run(AvroApplication.class, args);
	}

}
