package com.microservices.mentoring.checkout.controller;

import com.microservices.mentoring.checkout.controller.dto.PurchaseDto;
import com.microservices.mentoring.checkout.service.CheckoutService;
import com.microservices.mentoring.checkout.service.DiscountService;
import com.microservices.mentoring.checkout.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class CheckoutController {

    @Value("${mailer_hostname}")
    private String mailerHostname;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private PriceService priceService;

    private Map<String, PurchaseDto> cart = new HashMap<>();


    @PostMapping("/cart")
    public List<PurchaseDto> addToCart(@RequestParam String productId, @RequestParam int quantity) {
        if (null == cart.computeIfPresent(productId, (id, q) -> q.changeQuantity(quantity))) {
            cart.put(productId, new PurchaseDto(productId, quantity));
        }

        return convertToList(cart);
    }

    @GetMapping("/cart")
    public List<PurchaseDto> getCart() {
        System.out.println("Get cart");
        return convertToList(cart);
    }

    @DeleteMapping("/cart")
    public List<PurchaseDto> removeFromCart(@RequestParam String productId, @RequestParam int quantity) {
        System.out.println("Remove from cart");

        if (productId == null || !cart.containsKey(productId)) {
            return convertToList(cart);
        }

        PurchaseDto updatedPurchase = cart.computeIfPresent(productId, (id, q) -> q.changeQuantity(-quantity));

        if ((updatedPurchase != null ? updatedPurchase.getQuantity() : 0) <= 0) {
            cart.remove(productId);
        }
        return convertToList(cart);

    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) String promocode,
                           @RequestParam String email) throws Exception {
        System.out.println("Checkout");

        DiscountService.DiscountValue discount = new DiscountService.DiscountValue(BigDecimal.ZERO, false);

        if (promocode != null) {
            discount = discountService.findDiscount(promocode);
        }

        cart = populatePrices();
        checkoutService.performCheckout(cart.values(), discount.getValue(), email);
        cart = new HashMap<>();

        if (discount.isFailed()) {
            System.out.println("Sending promo fail email");
            sendPromoFailMail(email);
            return "Completed with issues";
        }
        System.out.println("Sending success email");
        sendSuccessMail(email);
        return "Success";
    }


    private Map<String, PurchaseDto> populatePrices() throws Exception {
        String ids = cart.keySet().stream().reduce((s, s2) -> s + "," + s2)
                .orElseThrow(() -> new Exception("Nothing to checkout"));

        Map<String, BigDecimal> prices;
        try {
            prices = priceService.getPrices(ids);
        } catch (Exception e) {
            System.out.println("Prices not found");
            throw new Exception("Catalog not available");
        }


        if (prices.size() != cart.size()) {
            throw new Exception("Unknown products detected");
        }

        prices.forEach(
                (id, price) -> cart.get(id).setPrice(price.multiply(BigDecimal.valueOf(cart.get(id).getQuantity()))));

        return cart;
    }


    private void sendSuccessMail(String customerEmail) {
        Map<String, String> args = new HashMap<>();
        args.put("address", customerEmail);
        args.put("emailType", "SUCCESS");
        sendEmail(args);
    }

    private void sendPromoFailMail(String customerEmail) {
        Map<String, String> args = new HashMap<>();
        args.put("address", customerEmail);
        args.put("emailType", "PROMOCODE_FAILURE");
        sendEmail(args);
    }

    private void sendEmail(Map<String, String> args) {
        try {
            restTemplate.postForLocation("http://" + mailerHostname + "/notification", args);
        } catch (Exception e) {
            System.out.println("Cannot send an email: " + e.getMessage());
        }
    }

    private List<PurchaseDto> convertToList(Map<String, PurchaseDto> cart) {
        return new LinkedList<>(cart.values());
    }

}
