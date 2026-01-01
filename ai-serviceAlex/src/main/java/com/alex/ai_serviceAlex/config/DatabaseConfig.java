package com.alex.ai_serviceAlex.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;

@Configuration
public class DatabaseConfig {

    /**
     * Initialize database schema on startup (optional, for development)
     * For production, use Flyway or Liquibase migrations
     */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        // Uncomment to auto-initialize schema (use with caution in production)
        // ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // populator.addScript(new ClassPathResource("schema.sql"));
        // initializer.setDatabasePopulator(populator);

        return initializer;
    }
}
