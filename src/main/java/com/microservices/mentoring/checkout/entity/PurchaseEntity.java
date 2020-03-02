package com.microservices.mentoring.checkout.entity;

//import javax.persistence.*;

//@Entity
//@Table(name = "purchase")
public class PurchaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(name = "id")
    private long id;

//    @Column(name = "product_id")
    private String productId;

//    @Column(name = "quantity")
    private int quantity;

//    @Column(name = "checkout_id")
//    private long checkoutId;

//    @ManyToOne
//    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutEntity checkoutEntity;

/*    public long getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(long checkoutId) {
        this.checkoutId = checkoutId;
    }*/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CheckoutEntity getCheckoutEntity() {
        return checkoutEntity;
    }

    public void setCheckoutEntity(CheckoutEntity checkoutEntity) {
        this.checkoutEntity = checkoutEntity;
    }
}
