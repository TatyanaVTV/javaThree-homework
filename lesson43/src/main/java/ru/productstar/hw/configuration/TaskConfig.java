package ru.productstar.hw.configuration;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
@ComponentScan("ru.productstar")
public class TaskConfig {

    @Bean
    public DataSource dataSource(
            @Value("${jdbcUrl}") String jdbcUrl,
            @Value("${jdbcUser}") String jdbcUser,
            @Value("${jdbcPswd}") String jdbcPswd
    ) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(jdbcUrl);
        dataSource.setUser(jdbcUser);
        dataSource.setPassword(jdbcPswd);
        return dataSource;
    }

    @Bean
    public CommandLineRunner cmd(DataSource dataSource) {
        return args -> {
            try (InputStream is = this.getClass().getResourceAsStream("/initial.sql")) {
                String sql = new String(is.readAllBytes());
                try (
                        Connection connection = dataSource.getConnection();
                        Statement statement = connection.createStatement()
                ) {
                    statement.executeUpdate(sql);
                }
            } catch (IOException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}