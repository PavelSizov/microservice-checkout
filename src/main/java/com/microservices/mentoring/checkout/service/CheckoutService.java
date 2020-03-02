package com.microservices.mentoring.checkout.service;

import com.microservices.mentoring.checkout.controller.dto.PurchaseDto;
import com.microservices.mentoring.checkout.entity.CheckoutEntity;
import com.microservices.mentoring.checkout.entity.PurchaseEntity;
import com.microservices.mentoring.checkout.repository.CheckoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service
public class CheckoutService {

//    @Autowired
    private CheckoutRepository checkoutRepository = new CheckoutRepository() {
    };

    public void performCheckout(Collection<PurchaseDto> purchases, BigDecimal discount, String customerEmail) {

        List<PurchaseEntity> purchaseEntities = convertFromDto(purchases);
        BigDecimal calculatedPrice = calculatePrice(purchases);

        BigDecimal totalPrice = calculateFinalPrice(calculatedPrice, discount);

        CheckoutEntity checkoutEntity =
                createCheckoutEntity(purchaseEntities, calculatedPrice, discount, totalPrice, customerEmail);

        saveCheckout(checkoutEntity);
    }

    //@Transactional
    private void saveCheckout(CheckoutEntity checkoutEntity) {
       // checkoutRepository.save(checkoutEntity);
    }

    private List<PurchaseEntity> convertFromDto(Collection<PurchaseDto> purchases) {

        List<PurchaseEntity> entities = new LinkedList<>();
        for (PurchaseDto dto : purchases) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setProductId(dto.getId());
            purchaseEntity.setQuantity(dto.getQuantity());
            entities.add(purchaseEntity);
        }
        return entities;
    }


    private CheckoutEntity createCheckoutEntity(List<PurchaseEntity> purchaseEntities, BigDecimal calculatedPrice,
                                                BigDecimal discount, BigDecimal finalPrice, String email) {
        CheckoutEntity entity = new CheckoutEntity();
        entity.setCustomerEmail(email);
        entity.setPurchases(purchaseEntities);
        entity.setCalculatedPrice(calculatedPrice);
        entity.setPromocodeDiscount(discount);
        entity.setFinalPrice(finalPrice);

        return entity;
    }

    private BigDecimal calculatePrice(Collection<PurchaseDto> purchases) {
        return purchases.stream().map(PurchaseDto::getPrice).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateFinalPrice(BigDecimal calculatedPrice, BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return calculatedPrice;
        }
        return calculatedPrice.subtract(calculatedPrice.multiply(discount));
    }
}
