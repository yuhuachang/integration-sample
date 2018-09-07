package com.example.integrationsample.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpStreamingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * Integration flow that read files from FTP without download file.
 */
//@Configuration
public class FtpGet {

    @Autowired
    private SessionFactory<FTPFile> ftpSessionFactory;

    @Bean
    @InboundChannelAdapter(channel = "inboundFtpChannel", poller = @org.springframework.integration.annotation.Poller(fixedRate = "5000", maxMessagesPerPoll = "-1"))
    public FtpStreamingMessageSource ftpMessageSource() {
        RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<FTPFile>(ftpSessionFactory);
        FtpStreamingMessageSource source = new FtpStreamingMessageSource(template);
        source.setRemoteDirectory("input");
        //source.setFilter(new SimplePatternFileListFilter(""));
        source.setMaxFetchSize(2);
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "inboundFtpChannel")
    public MessageHandler ftpInboundHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                Object payload = message.getPayload();

                // read message content
                if (payload instanceof InputStream) {
                    InputStream inputStream = (InputStream) message.getPayload();
                    
                    Thread t = Thread.currentThread();
                    System.out.println(t.getId() + " start...");
                    try {
                        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        System.out.println(t.getId() + " " + content);
                        t.sleep(1000);
                        
                        if (false) {
                            throw new RuntimeException("oh no!");
                        }
                        
                        System.out.println(t.getId() + " end.");
                        
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        throw new MessagingException(message, e);
                    }
                }
            }
        };
    }

}
