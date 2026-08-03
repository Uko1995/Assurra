package com.uko.eaas.identity.repository;

import com.uko.eaas.identity.model.entity.KycSubmission;
import com.uko.eaas.identity.model.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycSubmissionRepository extends JpaRepository<KycSubmission, UUID> {

    Optional<KycSubmission> findByMerchantId(UUID merchantId);

    Page<KycSubmission> findByStatus(KycStatus status, Pageable pageable);

    boolean existsByMerchantId(UUID merchantId);

    long countByStatus(KycStatus status);
}
