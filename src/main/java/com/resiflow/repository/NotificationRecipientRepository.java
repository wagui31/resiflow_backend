package com.resiflow.repository;

import com.resiflow.entity.NotificationRecipient;
import com.resiflow.entity.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    @Query("""
            select recipient
            from NotificationRecipient recipient
            join fetch recipient.notification notification
            where recipient.user.id = :userId
              and (:unreadOnly = false or recipient.read = false)
              and (:typesEmpty = true or notification.type in :types)
            order by notification.createdAt desc, recipient.id desc
            """)
    List<NotificationRecipient> findRecentForUser(
            @Param("userId") Long userId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("types") List<NotificationType> types,
            @Param("typesEmpty") boolean typesEmpty,
            Pageable pageable
    );

    @Query("""
            select count(recipient)
            from NotificationRecipient recipient
            join recipient.notification notification
            where recipient.user.id = :userId
              and recipient.read = false
              and (:typesEmpty = true or notification.type in :types)
            """)
    long countUnreadForUser(
            @Param("userId") Long userId,
            @Param("types") List<NotificationType> types,
            @Param("typesEmpty") boolean typesEmpty
    );

    Optional<NotificationRecipient> findByNotification_IdAndUser_Id(Long notificationId, Long userId);

    @Query("""
            select recipient
            from NotificationRecipient recipient
            join fetch recipient.notification notification
            where recipient.user.id = :userId
              and recipient.read = false
              and (:typesEmpty = true or notification.type in :types)
            """)
    List<NotificationRecipient> findUnreadForUser(
            @Param("userId") Long userId,
            @Param("types") List<NotificationType> types,
            @Param("typesEmpty") boolean typesEmpty
    );
}
