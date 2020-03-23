package com.microservices.mentoring.checkout.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class DiscountService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${discount_hostname}")
    private String discountHostname;

    @CircuitBreaker(maxAttempts = 3, openTimeout = 15000L, resetTimeout = 30000L)
    public DiscountValue findDiscount(String promocode) throws Exception {
        BigDecimal discount = restTemplate
                .getForObject("http://" + discountHostname + "/promocode/{promocode}", BigDecimal.class, promocode);

        if (discount == null || discount.compareTo(BigDecimal.ONE) > 0 || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Discount value is invalid: " + discount);
        }

        return new DiscountValue(discount, false);
    }

    @Recover
    private DiscountValue findDiscountFallback(String promocode) {
        return new DiscountValue(BigDecimal.ZERO, true);
    }

    public static class DiscountValue {

        private BigDecimal value;
        private boolean failed;

        public DiscountValue(BigDecimal value, boolean failed) {
            this.value = value;
            this.failed = failed;
        }

        public BigDecimal getValue() {
            return value;
        }

        public boolean isFailed() {
            return failed;
        }
    }
}
