package com.microservices.mentoring.checkout.repository;

import com.microservices.mentoring.checkout.entity.CheckoutEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutRepository extends CrudRepository<CheckoutEntity, Long> {
}
