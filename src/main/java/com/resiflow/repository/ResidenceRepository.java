package com.resiflow.repository;

import com.resiflow.entity.Residence;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidenceRepository extends JpaRepository<Residence, Long> {

    Optional<Residence> findByCode(String code);

    boolean existsByCode(String code);
}
