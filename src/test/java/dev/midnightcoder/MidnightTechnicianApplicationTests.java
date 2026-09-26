package dev.midnightcoder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MidnightTechnicianApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void isValidSpringModulith() {
        var modules = ApplicationModules.of(MidnightTechnician.class).verify();

        IO.println(modules.toString());
    }
}
