package com.magicapp.repository;

import com.magicapp.domain.PlayerParticipation;
import com.magicapp.domain.Tournament;
import com.magicapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
//    List<Tournament> findByOwnerId(Long ownerId);
    Tournament findByTournamentString(String tournamentString);
    Tournament findByOwner(User user);
    boolean existsByTournamentString(String tournamentString);
    boolean existsByOwner(User user);

}
