package com.magicapp.service;

import com.magicapp.domain.Tournament;
import com.magicapp.domain.User;
import com.magicapp.repository.TournamentRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TournamentService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private TournamentRepository tournamentRepository;
    @Autowired
    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public Tournament addNewTournament(User user){
//        if(tournamentString.isEmpty()){
//            throw new IllegalArgumentException("Tournament string is empty");
//        }
//        if(tournamentRepository.existsByTournamentString(tournamentString)){
//            throw new IllegalArgumentException("Tournament string exists");
//        }
        if(tournamentRepository.existsByOwner(user) && !tournamentRepository.findByOwner(user).isFinished()){
            throw new IllegalArgumentException("You're already hosting a tournament!");
        }
        Tournament tournament = new Tournament();
        tournament.setOwner(user);
        tournamentRepository.save(tournament); //idk why this works
        tournament.addPlayer(user);
        tournament.setTournamentString(generateTournamentString());
        tournamentRepository.save(tournament);
        return tournament;
    }

    public Tournament saveTournament(Tournament tournament){
        return tournamentRepository.save(tournament);
    }

    public Tournament findByTournamentString(String tournamentString){
        return tournamentRepository.findByTournamentString(tournamentString);
    }

    private String generateTournamentString(){
        String tournamentString;
        do{
            tournamentString = RandomStringUtils.randomAlphanumeric(5);
        }while(tournamentRepository.existsByTournamentString(tournamentString));
            return tournamentString;
    }
}
