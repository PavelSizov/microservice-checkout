package com.microservices.mentoring.checkout.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

@Entity
@Table(name = "checkout")
public class CheckoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "calculated_price")
    private BigDecimal calculatedPrice;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Column(name = "promocode_discount")
    private BigDecimal promocodeDiscount;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "checkoutEntity")
    private List<PurchaseEntity> purchases;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getCalculatedPrice() {
        return calculatedPrice;
    }

    public void setCalculatedPrice(BigDecimal calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public BigDecimal getPromocodeDiscount() {
        return promocodeDiscount;
    }

    public void setPromocodeDiscount(BigDecimal promocodeDiscount) {
        this.promocodeDiscount = promocodeDiscount;
    }

    public List<PurchaseEntity> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<PurchaseEntity> purchases) {
        for (PurchaseEntity purchase : purchases) {
            purchase.setCheckoutEntity(this);
        }
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CheckoutEntity.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("customerEmail='" + customerEmail + "'")
                .add("calculatedPrice=" + calculatedPrice)
                .add("finalPrice=" + finalPrice)
                .add("promocodeDiscount=" + promocodeDiscount)
                .add("purchases=" + purchases)
                .toString();
    }


}
