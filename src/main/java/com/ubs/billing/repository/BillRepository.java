package com.ubs.billing.repository;

import com.ubs.billing.entity.Bill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillReference(String billReference);

    boolean existsByReadingId(Long readingId);

    List<Bill> findByCustomerId(Long customerId);
}
