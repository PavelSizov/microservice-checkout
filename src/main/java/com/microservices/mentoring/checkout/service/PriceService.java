package com.microservices.mentoring.checkout.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PriceService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${catalog_hostname}")
    private String catalogHostname;

    @CircuitBreaker(maxAttempts = 3, openTimeout = 15000L, resetTimeout = 30000L)
    public Map<String, BigDecimal> getPrices(String productIds) throws Exception {
        System.out.println("Getting current prices");
        @SuppressWarnings("unchecked assignment")
        Map<String, Integer> prices = restTemplate
                .getForObject("http://" + catalogHostname + "/catalog/products/price?id={productIds}", Map.class,
                        productIds);

        if (prices == null || prices.size() == 0) {
            throw new Exception("No prices found");
        }

        Map<String, BigDecimal> pricesBD = new HashMap<>(prices.size());
        prices.forEach((s, integer) -> pricesBD.put(s, BigDecimal.valueOf(integer)));

        return pricesBD;
    }
}
