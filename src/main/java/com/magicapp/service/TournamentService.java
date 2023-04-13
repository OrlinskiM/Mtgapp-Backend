package com.magicapp.service;

import com.magicapp.domain.*;
import com.magicapp.exception.domain.TournamentNotFoundException;
import com.magicapp.repository.TournamentRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.magicapp.constant.TournamentConstant.*;

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
        tournament.setCurrentRound(0);
        tournament.setCreationDate(new Date());
        tournamentRepository.save(tournament); //idk why this works
        tournament.createParticipationForPlayer(user);
        tournament.setTournamentString(generateTournamentString());
        tournamentRepository.save(tournament);
        return tournament;
    }

    public Tournament startTournament(Tournament tournament, int rounds){
        tournament.setRounds(rounds);
        tournament.pairNextRound();
        return tournamentRepository.save(tournament);
    }

    public Tournament pairNextRoundInTournament(Tournament tournament){
        tournament.pairNextRound();
        return tournamentRepository.save(tournament);
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

    public Tournament addPlayer(Tournament tournament, Player player){
        tournament.createParticipationForPlayer(player);
        return tournamentRepository.save(tournament);
    }

    public Tournament addGameResults(Tournament tournament, int gameId, int scorePlayer1, int scorePlayer2){
        List<Game> allGames = tournament.getAllGames();
        for (Game oldGame: allGames) {
            if(oldGame.getGameId() == gameId){
                oldGame.setGamesWonPlayer1(scorePlayer1);
                oldGame.setGamesWonPlayer2(scorePlayer2);
                oldGame.calculateResult();
            }
        }
        tournament.setAllGames(allGames);
        return tournamentRepository.save(tournament);
    }

    public Tournament validateUserInTournament (Tournament tournament, User user){
        if(isPlayerParticipating(tournament, user)){
            return tournament;
        }
        tournament.createParticipationForPlayer(user);
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
