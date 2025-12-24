package ru.mirakyan.mymarket.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@Profile("test")
public class TestR2dbcConfig {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder
                .withUrl("r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")
                .username("sa")
                .password("")
                .build();
    }

    @Bean
    public ConnectionFactoryInitializer testInitializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }
}
