package com.resiflow.repository;

import com.resiflow.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
}
