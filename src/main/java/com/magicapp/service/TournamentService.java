package com.magicapp.service;

import com.magicapp.domain.Player;
import com.magicapp.domain.PlayerParticipation;
import com.magicapp.domain.Tournament;
import com.magicapp.domain.User;
import com.magicapp.exception.domain.TournamentNotFoundException;
import com.magicapp.repository.TournamentRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.magicapp.constant.TournamentConstant.NO_TOURNAMENT_FOUND_BY_STRING;
import static com.magicapp.constant.TournamentConstant.USER_NOT_TOURNAMENT_OWNER;

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
//        if(tournamentRepository.existsByOwner(user) && !tournamentRepository.findByOwner(user).isFinished()){
//            throw new IllegalArgumentException("You're already hosting a tournament!");
//        }
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

    public Tournament findByTournamentString(String tournamentString) throws TournamentNotFoundException {
        if(!tournamentRepository.existsByTournamentString(tournamentString)){
            throw new TournamentNotFoundException(NO_TOURNAMENT_FOUND_BY_STRING + tournamentString);
        }
        Tournament tournament = tournamentRepository.findByTournamentString(tournamentString);
        return tournament;
    }

    public boolean isPlayerParticipating(Tournament tournament, Player player){
        List<PlayerParticipation> participations = tournament.getParticipations();
        for (PlayerParticipation participation: participations) {
            if(participation.getPlayer().getUserId() == player.getUserId()){
                return true;
            }
        }
        return false;
    }

    public Tournament addPlayerToTournament(Tournament tournament, Player player){
        tournament.addPlayer(player);
        return tournamentRepository.save(tournament);
    }

    public Tournament validateUserInTournament (Tournament tournament, User user){
        if(isPlayerParticipating(tournament, user)){
            return tournament;
        }
        tournament.addPlayer(user);
        tournamentRepository.save(tournament);
        return tournament;
    }

    public Tournament validateOwnerInTournament(Tournament tournament, User user){
        if(tournament.getOwner().getUserId() != user.getUserId()){
            throw new IllegalArgumentException(USER_NOT_TOURNAMENT_OWNER);
        }
        return tournament;
    }

    private String generateTournamentString(){
        String tournamentString;
        do{
            tournamentString = RandomStringUtils.randomAlphanumeric(5);
        }while(tournamentRepository.existsByTournamentString(tournamentString));
            return tournamentString;
    }
}
