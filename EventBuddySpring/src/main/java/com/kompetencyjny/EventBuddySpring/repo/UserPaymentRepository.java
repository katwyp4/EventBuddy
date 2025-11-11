package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.UserPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, Long> {

    Optional<UserPayment> findByStripePaymentIntentId(String id);
}
