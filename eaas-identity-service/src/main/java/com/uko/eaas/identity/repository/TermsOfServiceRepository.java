package com.uko.eaas.identity.repository;

import com.uko.eaas.identity.model.entity.TermsOfService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermsOfServiceRepository extends JpaRepository<TermsOfService, UUID> {

    Optional<TermsOfService> findFirstByIsActiveTrueOrderByEffectiveFromDesc();
}
