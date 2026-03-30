package com.resiflow.repository;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByResidence_Id(Long residenceId);

    List<User> findAllByResidence_IdAndStatus(Long residenceId, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByResidence_IdAndRole(Long residenceId, UserRole role);

    Optional<User> findByIdAndResidence_Id(Long id, Long residenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id and user.residence.id = :residenceId")
    Optional<User> findByIdAndResidence_IdForUpdate(@Param("id") Long id, @Param("residenceId") Long residenceId);
}
