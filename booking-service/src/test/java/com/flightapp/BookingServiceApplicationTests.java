package com.flightapp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Context load test disabled for microservice")
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
