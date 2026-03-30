package com.resiflow.repository;

import com.resiflow.entity.VoteChoix;
import com.resiflow.entity.VoteUtilisateur;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteUtilisateurRepository extends JpaRepository<VoteUtilisateur, Long> {

    boolean existsByVote_IdAndUtilisateur_Id(Long voteId, Long utilisateurId);

    void deleteByVote_IdAndUtilisateur_Id(Long voteId, Long utilisateurId);

    long countByVote_IdAndChoix(Long voteId, VoteChoix choix);

    List<VoteUtilisateur> findAllByVote_IdOrderByDateVoteAsc(Long voteId);
}
