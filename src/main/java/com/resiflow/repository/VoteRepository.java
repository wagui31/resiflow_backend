package com.resiflow.repository;

import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteStatut;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findAllByResidence_IdOrderByDateDebutDesc(Long residenceId);

    List<Vote> findAllByStatutAndDateFinBefore(VoteStatut statut, LocalDateTime dateTime);
}
