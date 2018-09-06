package com.example.integrationsample.flow;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Integration flow that put files to FTP triggered by scheduler.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class FtpPut {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SessionFactory<FTPFile> ftpSessionFactory;

//    @Bean
//    @ServiceActivator(inputChannel = "toFtpChannel")
    public MessageHandler ftpOutboundHandler() {
        FtpMessageHandler handler = new FtpMessageHandler(ftpSessionFactory);
        handler.setCharset(StandardCharsets.UTF_8.name());
        handler.setRemoteDirectoryExpressionString("'/archive'");
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {

                // create file name using current timestamp
                Instant instant = Instant.now();
                long timeStampMillis = instant.toEpochMilli();
                return timeStampMillis + ".txt";
            }
        });
        handler.setUseTemporaryFileName(true);
        // handler.setChmod(644);
        return handler;
    }

//    @MessagingGateway
    public interface MyGateway {

        @Gateway(requestChannel = "toFtpChannel")
        void sendToFtp(String fileName);
    }

//    @Scheduled(fixedDelay = 5000)
    public void putToFtp() {
        MyGateway gateway = context.getBean(MyGateway.class);
        
        // upload file. accept 1) java.io.File; 2) byte[]; and 3) java.lang.String.
        gateway.sendToFtp("test123");
    }

}
