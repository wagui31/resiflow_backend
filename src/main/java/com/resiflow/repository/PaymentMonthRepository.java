package com.resiflow.repository;

import com.resiflow.entity.PaymentMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMonthRepository extends JpaRepository<PaymentMonth, Long> {

    List<PaymentMonth> findAllByUser_IdOrderByMonthAsc(Long userId);

    Optional<PaymentMonth> findByUser_IdAndMonth(Long userId, String month);
}
