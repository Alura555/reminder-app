package com.github.alura.reminder.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;


@TestConfiguration
public class TestcontainersConfiguration {
	@Bean(initMethod = "start", destroyMethod = "stop")
	@ServiceConnection
	public PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
				.withDatabaseName("testdb")
				.withUsername("test")
				.withPassword("test");
	}

	@Bean
	@Primary
	public DataSource dataSource(PostgreSQLContainer<?> postgresContainer) {
		return DataSourceBuilder.create()
				.url(postgresContainer.getJdbcUrl())
				.username(postgresContainer.getUsername())
				.password(postgresContainer.getPassword())
				.driverClassName(postgresContainer.getDriverClassName())
				.build();
	}
}