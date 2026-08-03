package com.uko.eaas.escrow.repository;

import com.uko.eaas.escrow.model.entity.EscrowStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EscrowStateHistoryRepository extends JpaRepository<EscrowStateHistory, UUID> {

    List<EscrowStateHistory> findByEscrowIdOrderByCreatedAtDesc(UUID escrowId);

    List<EscrowStateHistory> findByTriggeredById(UUID triggeredById);
}
