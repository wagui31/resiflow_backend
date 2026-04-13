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

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    List<User> findAllByResidence_Id(Long residenceId);

    List<User> findAllByResidence_IdAndStatus(Long residenceId, UserStatus status);

    List<User> findAllByResidence_IdAndStatusAndRoleIn(Long residenceId, UserStatus status, List<UserRole> roles);

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByResidence_IdAndRole(Long residenceId, UserRole role);

    List<User> findAllByLogement_Id(Long logementId);

    List<User> findAllByLogement_IdAndStatus(Long logementId, UserStatus status);

    long countByResidence_IdAndRole(Long residenceId, UserRole role);

    long countByLogement_IdAndStatusAndRoleIn(Long logementId, UserStatus status, List<UserRole> roles);

    long countByResidence_IdAndStatusAndRoleIn(Long residenceId, UserStatus status, List<UserRole> roles);

    Optional<User> findByIdAndResidence_Id(Long id, Long residenceId);

    Optional<User> findByEmailAndResidence_Id(String email, Long residenceId);

    Optional<User> findByEmailAndResidence_IdAndStatus(String email, Long residenceId, UserStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id and user.residence.id = :residenceId")
    Optional<User> findByIdAndResidence_IdForUpdate(@Param("id") Long id, @Param("residenceId") Long residenceId);
}
