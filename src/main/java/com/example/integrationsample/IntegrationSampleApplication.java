package com.example.integrationsample;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class IntegrationSampleApplication {

	public static void main(String[] args) {
	    new SpringApplicationBuilder(IntegrationSampleApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
	}
}
