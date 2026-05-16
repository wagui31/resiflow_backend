package com.resiflow.repository;

import com.resiflow.entity.PushTokenStatus;
import com.resiflow.entity.UserPushToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPushTokenRepository extends JpaRepository<UserPushToken, Long> {

    Optional<UserPushToken> findByToken(String token);

    List<UserPushToken> findAllByInstallationId(String installationId);

    List<UserPushToken> findAllByUser_Id(Long userId);

    List<UserPushToken> findAllByUser_IdAndStatus(Long userId, PushTokenStatus status);

    List<UserPushToken> findAllByUser_IdInAndStatus(Collection<Long> userIds, PushTokenStatus status);

    Optional<UserPushToken> findByUser_IdAndToken(Long userId, String token);

    List<UserPushToken> findAllByUser_IdAndInstallationId(Long userId, String installationId);

    void deleteAllByUser_Id(Long userId);
}
