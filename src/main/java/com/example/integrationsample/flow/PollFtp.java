package com.example.integrationsample.flow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.batch.poller.Poller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ImageBanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.dsl.FtpInboundChannelAdapterSpec;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.ReflectionUtils;

/**
 * @see <a href=
 *      "https://docs.spring.io/spring-integration/reference/html/ftp.html">reference</a>
 */
@Configuration
public class PollFtp {

    @Value("${ftp-host}")
    String host;
    @Value("${ftp-port:21}")
    int port;
    @Value("${ftp-username}")
    String username;
    @Value("${ftp-password}")
    String password;

    @Bean
    public SessionFactory<FTPFile> ftpSessionFactory() {
        DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
        sessionFactory.setHost(host);
        sessionFactory.setPort(port);
        sessionFactory.setUsername(username);
        sessionFactory.setPassword(password);
        return new CachingSessionFactory<FTPFile>(sessionFactory);
    }

    @Bean
    public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
        FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(ftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(false);
        fileSynchronizer.setRemoteDirectory("input");
        fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*.xml"));
        return fileSynchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "ftpChannel", poller = @org.springframework.integration.annotation.Poller(fixedDelay = "5000"))
    public MessageSource<File> ftpMessageSource() {
        FtpInboundFileSynchronizingMessageSource source = new FtpInboundFileSynchronizingMessageSource(
                ftpInboundFileSynchronizer());
        // source.setLocalDirectory(new File("ftp-inbound"));
        // source.setAutoCreateLocalDirectory(true);
        // source.setLocalFilter(new AcceptOnceFileListFilter<File>());
        source.setMaxFetchSize(1);
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "ftpChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println(message.getPayload());
            }
        };
    }

    //
    // @Bean
    // IntegrationFlow pollFtpFlow(DefaultFtpSessionFactory sessionFactory) {
    // FtpInboundChannelAdapterSpec inbound =
    // Ftp.inboundAdapter(sessionFactory);
    // inbound.remoteDirectory("in");
    // inbound.preserveTimestamp(true);
    // inbound.deleteRemoteFiles(false);
    //
    // FtpInboundFileSynchronizingMessageSource source = inbound.get();
    //
    // IntegrationFlowBuilder builder = IntegrationFlows.from(source, poller ->
    // poller.poller(Pollers.fixedDelay(1000)));
    // builder.handle
    //
    // return builder.get();
    //
    // return IntegrationFlows.from(inputSource, poller ->
    // poller.poller(Pollers.fixedDelay(1000)))
    // .transform(File.class, (GenericTransformer<File, String>) (File source)
    // -> {
    // try {
    // try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
    // PrintStream printStream = new PrintStream(outStream);
    // ImageBanner imageBanner = new ImageBanner(new
    // FileSystemResource(source));
    // imageBanner.printBanner(null, getClass(), printStream);
    // Message<String> message = MessageBuilder.withPayload(new
    // String(outStream.toByteArray()))
    // .setHeader(FileHeaders.FILENAME, source.getName()).build();
    // return message.getPayload();
    // }
    // } catch (IOException e) {
    // ReflectionUtils.rethrowRuntimeException(e);
    // }
    // return null;
    // }).handleWithAdapter(adapters ->
    // adapters.ftp(ftpSessionFactory).fileNameGenerator(message -> {
    // Object o = message.getHeaders().get(FileHeaders.FILENAME);
    // String fileName = String.class.cast(o);
    // return fileName + ".txt";
    // }).autoCreateDirectory(true).remoteDirectory("/out")).get();
    // }
}
