package com.microservices.mentoring.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.jdbc.config.annotation.EnableRdsInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaRepositories
@EnableRdsInstance(dbInstanceIdentifier = "${db_instance}", databaseName = "${db_name}", username = "${db_username}", password = "${db_password}")
@EnableRetry
public class CheckoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(getClientHttpRequestFactory());
    }

    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(10_000);

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(10_000);
        return clientHttpRequestFactory;
    }
}
