package com.uko.eaas.payment.repository;

import com.uko.eaas.payment.model.entity.AmlAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, UUID> {

    Page<AmlAlert> findByStatus(String status, Pageable pageable);

    List<AmlAlert> findByCustomerId(UUID customerId);

    List<AmlAlert> findByMerchantId(UUID merchantId);
}
