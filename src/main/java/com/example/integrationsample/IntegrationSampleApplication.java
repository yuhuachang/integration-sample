package com.example.integrationsample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
public class IntegrationSampleApplication {
    private static Log logger = LogFactory.getLog(IntegrationSampleApplication.class);
    
	public static void main(String[] args) {
	    new SpringApplicationBuilder(IntegrationSampleApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
	    
//	    AbstractApplicationContext context = new ClassPathXmlApplicationContext("/flow/SimpleFlow.xml", IntegrationSampleApplication.class);
//        MessageChannel inputChannel = context.getBean("inputChannel", MessageChannel.class);
//        PollableChannel outputChannel = context.getBean("outputChannel", PollableChannel.class);
//        inputChannel.send(new GenericMessage<String>("World"));
//        logger.info("==> HelloWorldDemo: " + outputChannel.receive(0).getPayload());
//        context.close();
	}
}
