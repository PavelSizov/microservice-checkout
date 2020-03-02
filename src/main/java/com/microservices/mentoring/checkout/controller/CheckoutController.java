package com.microservices.mentoring.checkout.controller;

import com.microservices.mentoring.checkout.controller.dto.PurchaseDto;
import com.microservices.mentoring.checkout.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
    public String checkout(@RequestParam String promocode, @RequestParam String email) throws Exception {
        BigDecimal discount;
        boolean promoFail = false;
        try {
            discount = findDiscount(promocode);
        } catch (RestClientException rce) {
            discount = BigDecimal.ZERO;
            promoFail = true;
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

    @GetMapping("/test/discount")
    public String testConnect(@RequestParam String promocode) {
        BigDecimal discount = restTemplate
                .getForObject("http://" + discountHostname + "/promocode/{promocode}", BigDecimal.class, promocode);

        return discount.toString();
    }

    @GetMapping("/test/values")
    public String testValues() {
        return new StringJoiner("\n").add(catalogHostname).add(mailerHostname).add(discountHostname).toString();

    }

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

        Map<String, BigDecimal> prices = restTemplate
                .getForObject("http://" + catalogHostname + "/catalog/products/price?id={productIds}", Map.class, ids);

        if (prices == null) {
            throw new Exception("No prices found");
        }

        prices.forEach((id, price) -> cart.get(id).setPrice(price));

        return cart;
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
