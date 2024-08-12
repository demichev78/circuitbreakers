package com.capitalone.resiliency;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ClientTest {

	@Autowired
	private Client myService;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	private MockWebServer mockWebServer;

	@BeforeEach
	public void setup() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterEach
	public void teardown() throws Exception {
		mockWebServer.shutdown();
	}


	@Test
	public void whenServiceFails_thenCircuitBreakerRetriesAndFallback()  {
		// Arrange
		for (var i = 0; i < 50; i++) mockWebServer.enqueue(new MockResponse().setResponseCode(500)); // First response is a failure
		String baseUrl = mockWebServer.url("/").toString();


		for (int i = 0; i < 50; i++) {
			try {
				// Act
				String response = myService.callExternalService(baseUrl);
				// Assert
				assertEquals("Fallback response", response);
				Thread.sleep(200);
			} catch (Exception e) {
				// Assert
				System.out.println("Exception: " + e.getMessage());
			}
		}
		assertTrue(mockWebServer.getRequestCount() < 10); // Circuit breaker should have opened so we will get less than 10 retries while a for loop has 50 iterations
	}
}