package com.example.citymanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("localhost") //pg
    private String host;

    @Value("5432") //5432
    private String port;

    @Value("is_lab1") //studs
    private String database;

    @Value("postgres") //s409517
    private String username;

    @Value("root") //O2SrXNfNxzyfo9sl
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Проверяем, что все переменные окружения заданы
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

}
