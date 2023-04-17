package com.magicapp.repository;

import com.magicapp.domain.PlayerParticipation;
import com.magicapp.domain.Tournament;
import com.magicapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
//    List<Tournament> findByOwnerId(Long ownerId);
    Tournament findByTournamentString(String tournamentString);
    Tournament findByOwner(User user);
    @Query("SELECT t\n" +
            "FROM Tournament t\n" +
            "JOIN t.participations p\n" +
            "WHERE p.player.userId = ?1")
    Tournament[] findAllByUserId(Long userId);
    boolean existsByTournamentString(String tournamentString);
    boolean existsByOwner(User user);

}
