package com.microservices.mentoring.checkout.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Objects;


public class PurchaseDto {

    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal price;

    private int quantity;

    public PurchaseDto(String id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public PurchaseDto changeQuantity(int modificator) {
        quantity += modificator;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseDto that = (PurchaseDto) o;
        return quantity == that.quantity && id.equals(that.id) && price.equals(that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, quantity);
    }
}
