package com.ubs.billing.repository;

import com.ubs.billing.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByNationalId(String nationalId);

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findFirstByEmail(String email);
}
