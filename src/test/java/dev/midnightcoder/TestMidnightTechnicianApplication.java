package dev.midnightcoder;

import org.springframework.boot.SpringApplication;

public class TestMidnightTechnicianApplication {

	void main() {
		SpringApplication.from(MidnightTechnician::main).with(TestcontainersConfiguration.class).run();
	}

}
