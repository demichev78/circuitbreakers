package com.capitalone.resiliency;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@CircuitBreaker(name = "clientService", fallbackMethod = "fallbackMethod")
@Service
public class Client {

	@Autowired
	private RestTemplate restTemplate;


	public String callExternalService(String url) {
		try {
			return restTemplate.getForObject(url, String.class);
		} catch (Exception e) {
			throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Service is down");
		}
	}

	private String fallbackMethod(String something, Throwable throwable) {
		System.out.println("Fallback method called, cause: " + throwable.getMessage());
		return "Fallback response";
	}
}
