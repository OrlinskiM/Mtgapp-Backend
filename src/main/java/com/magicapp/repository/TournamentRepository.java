package com.magicapp.repository;

import com.magicapp.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
//    List<Tournament> findByOwnerId(Long ownerId);
    Tournament findByTournamentString(String tournamentString);

}
