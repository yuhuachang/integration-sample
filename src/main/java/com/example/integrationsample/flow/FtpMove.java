package com.example.integrationsample.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
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

    private String inputDirectory = "/input";
    private String archiveDirectory = "/archive";

//    <int-ftp:inbound-channel-adapter id="ftpInbound"
//            channel="ftpChannel"
//            session-factory="ftpClientFactory"
//            filename-pattern="*.txt"
//            auto-create-local-directory="true"
//            delete-remote-files="false"
//            remote-directory="/"
//            local-directory="#{ T(org.springframework.integration.samples.ftp.TestSuite).LOCAL_FTP_TEMP_DIR}/ftpInbound">
//        <int:poller fixed-rate="1000"/>
//</int-ftp:inbound-channel-adapter>


    @Bean
    @InboundChannelAdapter(channel = "inboundFtpChannel", poller = @org.springframework.integration.annotation.Poller(fixedRate = "5000"))
    public FtpStreamingMessageSource ftpMessageSource() {
        RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<FTPFile>(ftpSessionFactory);
        
        FtpStreamingMessageSource source = new FtpStreamingMessageSource(template);
        source.setRemoteDirectory(inputDirectory);
        source.setFilter(new FileListFilter<FTPFile>() {
            @Override
            public List<FTPFile> filterFiles(FTPFile[] files) {
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    Calendar fileTime = file.getTimestamp();
                    System.err.println(String.format("name: %s time: %s", fileName, fileTime.getTime()));
                }
                return Arrays.asList(files);
            }
        });
        source.setMaxFetchSize(10);
        source.setLoggingEnabled(true);
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "inboundFtpChannel", outputChannel = "outboundFtpChannel")
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
                        System.out.println("fetch: " + content);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new MessagingException(message, e);
                    }

                    // success...
                }
            }
        };
    }

    // @Bean
    // @InboundChannelAdapter(channel = "inFtpChannel", poller =
    // @org.springframework.integration.annotation.Poller(fixedDelay = "5000"))
    // public FtpStreamingMessageSource ftpMessageSource() {
    // RemoteFileTemplate<FTPFile> template = new
    // RemoteFileTemplate<FTPFile>(ftpSessionFactory);
    // FtpStreamingMessageSource source = new
    // FtpStreamingMessageSource(template);
    // source.setRemoteDirectory("input");
    // source.setMaxFetchSize(1);
    // return source;
    // }
    //
    // @Bean
    // @ServiceActivator(inputChannel = "outFtpChannel")
    // public MessageHandler ftpOutboundHandler() {
    // FtpMessageHandler handler = new FtpMessageHandler(ftpSessionFactory);
    // handler.setCharset(StandardCharsets.UTF_8.name());
    // handler.setRemoteDirectoryExpressionString("'/archive'");
    // handler.setFileNameGenerator(new FileNameGenerator() {
    // @Override
    // public String generateFileName(Message<?> message) {
    //
    // // create file name using current timestamp
    // Instant instant = Instant.now();
    // long timeStampMillis = instant.toEpochMilli();
    // return timeStampMillis + ".txt";
    // }
    // });
    // handler.setUseTemporaryFileName(true);
    // return handler;
    // }
    //
    // @Bean
    // @ServiceActivator(inputChannel = "inFtpChannel", outputChannel =
    // "outFtpChannel")
    // public MessageHandler ftpInboundHandler() {
    // return new MessageHandler() {
    // @Override
    // public void handleMessage(Message<?> message) throws MessagingException {
    // Object payload = message.getPayload();
    //
    // // read message content
    // if (payload instanceof InputStream) {
    // InputStream inputStream = (InputStream) message.getPayload();
    // try {
    // String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    // System.out.println(content);
    // } catch (IOException e) {
    // e.printStackTrace();
    // throw new MessagingException(message, e);
    // }
    // }
    // }
    // };
    // }

}
