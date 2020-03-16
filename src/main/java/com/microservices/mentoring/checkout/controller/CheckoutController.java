package com.microservices.mentoring.checkout.controller;

import com.microservices.mentoring.checkout.controller.dto.PurchaseDto;
import com.microservices.mentoring.checkout.service.CheckoutService;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CheckoutController {

    @Value("${catalog_hostname}")
    private String catalogHostname;

    @Value("${discount_hostname}")
    private String discountHostname;

    @Value("${mailer_hostname}")
    private String mailerHostname;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CheckoutService checkoutService;

    private Map<String, PurchaseDto> cart = new HashMap<>();

    @PostMapping("/cart")
    public Map<String, PurchaseDto> addToCart(@RequestParam String productId, @RequestParam int quantity) {
        if (null == cart.computeIfPresent(productId, (id, q) -> q.changeQuantity(quantity))) {
            cart.put(productId, new PurchaseDto(productId, quantity));
        }

        return cart;
    }

    @GetMapping("/cart")
    public Map<String, PurchaseDto> getCart() {
        return cart;
    }

    @DeleteMapping("/cart")
    public Map<String, PurchaseDto> removeFromCart(@RequestParam String productId, @RequestParam int quantity) {
        if (productId == null || !cart.containsKey(productId)) {
            return cart;
        }

        PurchaseDto updatedPurchase = cart.computeIfPresent(productId, (id, q) -> q.changeQuantity(-quantity));

        if ((updatedPurchase != null ? updatedPurchase.getQuantity() : 0) <= 0) {
            cart.remove(productId);
        }
        return cart;

    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) String promocode,
                           @RequestParam String email) throws Exception {
        BigDecimal discount = BigDecimal.ZERO;
        boolean promoFail = false;

        if (promocode != null) {
            try {
                discount = findDiscount(promocode);
            } catch (RestClientException rce) {
                promoFail = true;
            }
        }

        cart = populatePrices();
        checkoutService.performCheckout(cart.values(), discount, email);
        cart = new HashMap<>();

        if (promoFail) {
            sendPromoFailMail(email);
            return "Completed with issues";
        }
        sendSuccessMail(email);
        return "Success";
    }

    @Retryable
    private BigDecimal findDiscount(String promocode) throws Exception {
        BigDecimal discount = restTemplate
                .getForObject("http://" + discountHostname + "/promocode/{promocode}", BigDecimal.class, promocode);

        if (discount == null || discount.compareTo(BigDecimal.ONE) > 0 || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Discount value is invalid: " + discount);
        }

        return discount;
    }

    private Map<String, PurchaseDto> populatePrices() throws Exception {
        String ids = cart.keySet().stream().reduce((s, s2) -> s + "," + s2)
                .orElseThrow(() -> new Exception("Nothing to checkout"));

        Map<String, BigDecimal> prices = getPrices(ids);

        if (prices.size() != cart.size()) {
            throw new Exception("Unknown products detected");
        }

        prices.forEach(
                (id, price) -> cart.get(id).setPrice(price.multiply(BigDecimal.valueOf(cart.get(id).getQuantity()))));

        return cart;
    }

    @Retryable
    private Map<String, BigDecimal> getPrices(String productIds) throws Exception {

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

    private void sendSuccessMail(String customerEmail) {
        Map<String, String> args = new HashMap<>();
        args.put("address", customerEmail);
        args.put("emailType", "SUCCESS");
        restTemplate.postForLocation("http://" + mailerHostname + "/notification", args);
    }

    private void sendPromoFailMail(String customerEmail) {
        Map<String, String> args = new HashMap<>();
        args.put("address", customerEmail);
        args.put("emailType", "PROMOCODE_FAILURE");
        restTemplate.postForLocation("http://" + mailerHostname + "/notification", args);
    }
}
