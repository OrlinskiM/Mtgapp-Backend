package com.magicapp.resource;

import com.magicapp.domain.*;
import com.magicapp.repository.TournamentRepository;
import com.magicapp.repository.UserRepository;
import com.magicapp.service.TournamentService;
import com.magicapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "*")
@RequestMapping(path = { "/tournament"})
public class TournamentResource {

    private UserService userService;

    private TournamentService tournamentService;

    @Autowired
    public TournamentResource(TournamentService tournamentService, UserService userService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<Tournament> addNewTournament(){
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByUsername(currentUserName);
        Tournament tournament = tournamentService.addNewTournament(currentUser);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}")
    public ResponseEntity<Tournament> getTournament(@PathVariable("tournamentString") String tournamentString){
        Tournament getTournament = tournamentService.findByTournamentString(tournamentString);

        return new ResponseEntity<>(getTournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/update")
    public ResponseEntity<Tournament> updateTournament(@PathVariable("tournamentString") String tournamentString,
                                                 @RequestParam("userId") Long userId){
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User user = userService.findUserByUserId(userId);
        tournament.addPlayer(user);
        RoundMatching roundMatching = new RoundMatching(1);
        tournament.addRoundMatching(roundMatching);
        roundMatching.addGame(new Game(1,user, (Player) tournament.getPlayers().toArray()[0]));

////        Game game = new Game(2L, user, user);
//        tournament.addGame(game);
        tournamentService.saveTournament(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }
}
