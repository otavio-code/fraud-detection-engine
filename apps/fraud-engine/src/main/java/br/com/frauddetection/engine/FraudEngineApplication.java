package br.com.frauddetection.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FraudEngineApplication {

	public static void main(String[] args) {

		System.setProperty(
				"org.apache.avro.SERIALIZABLE_PACKAGES",
				"br.com.frauddetection.events"
		);

		SpringApplication.run(FraudEngineApplication.class, args);
	}
}