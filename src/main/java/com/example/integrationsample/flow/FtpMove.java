package com.example.integrationsample.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpStreamingMessageSource;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * Integration flow that read files from FTP, do some processing, then move to
 * another folder on the same FTP.
 */
@Configuration
public class FtpMove {

    @Autowired
    private SessionFactory<FTPFile> ftpSessionFactory;

    @Bean
    @InboundChannelAdapter(channel = "inFtpChannel", poller = @org.springframework.integration.annotation.Poller(fixedDelay = "5000"))
    public FtpStreamingMessageSource ftpMessageSource() {
        RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<FTPFile>(ftpSessionFactory);
        FtpStreamingMessageSource source = new FtpStreamingMessageSource(template);
        source.setRemoteDirectory("input");
        source.setMaxFetchSize(1);
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "outFtpChannel")
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
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "inFtpChannel", outputChannel = "outFtpChannel")
    public MessageHandler ftpInboundHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                Object payload = message.getPayload();

                // read message content
                if (payload instanceof InputStream) {
                    InputStream inputStream = (InputStream) message.getPayload();
                    try {
                        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        System.out.println(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new MessagingException(message, e);
                    }
                }
            }
        };
    }

}
