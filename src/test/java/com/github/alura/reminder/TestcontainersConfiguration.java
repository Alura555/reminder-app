package com.github.alura.reminder;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@TestConfiguration
public class TestcontainersConfiguration {

	static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	static {
		postgresContainer.start();
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
	}
}