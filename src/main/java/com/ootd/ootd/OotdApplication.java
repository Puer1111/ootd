package com.ootd.ootd;

import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

	@SpringBootApplication(exclude = {
			GcpContextAutoConfiguration.class,
			GcpStorageAutoConfiguration.class
	})
public class OotdApplication {


	public static void main(String[] args) {
		SpringApplication.run(OotdApplication.class, args);
	}

}
