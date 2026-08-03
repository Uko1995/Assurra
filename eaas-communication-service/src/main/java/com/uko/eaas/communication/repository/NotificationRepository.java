package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.Notification;
import com.uko.eaas.communication.model.enums.NotificationStatus;
import com.uko.eaas.communication.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    List<Notification> findByStatusAndSentAtBefore(NotificationStatus status, LocalDateTime sentAt);

    List<Notification> findByStatus(NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.type = :type")
    List<Notification> findPendingByType(@Param("type") NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = 'DELIVERED' AND n.readAt IS NULL")
    long countUnreadByUser(@Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt < :before")
    List<Notification> findStalePendingNotifications(@Param("before") LocalDateTime before);

    boolean existsByUserIdAndSourceEventId(UUID userId, String sourceEventId);
}
