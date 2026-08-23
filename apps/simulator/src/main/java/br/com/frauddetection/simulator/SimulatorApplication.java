package br.com.frauddetection.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimulatorApplication {

    public static void main(String[] args) {

        System.setProperty(
                "org.apache.avro.SERIALIZABLE_PACKAGES",
                "br.com.frauddetection.events"
        );

        SpringApplication.run(SimulatorApplication.class, args);
    }
}