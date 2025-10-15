package com.github.alura.reminder;

import org.springframework.boot.SpringApplication;

public class TestReminderApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReminderApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
