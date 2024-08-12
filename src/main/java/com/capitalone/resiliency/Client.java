package com.capitalone.resiliency;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class Client {



	@Autowired
	private RestTemplate restTemplate;

	private long lastInvocation = 0;

	@CircuitBreaker(name = "clientService", fallbackMethod = "fallbackMethod")
	@Retry(name = "clientService")
	public String callExternalService(String url) {
		try {
			var now = System.currentTimeMillis();
			if (lastInvocation != 0) System.out.println("Time since last invocation: " + (now - lastInvocation) + "ms");
			lastInvocation = now;
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
