package com.example.integrationsample.config;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

@Configuration
public class FtpConfig {

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
}
