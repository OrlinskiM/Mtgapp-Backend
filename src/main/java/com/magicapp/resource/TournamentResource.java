package com.magicapp.resource;

import com.magicapp.domain.*;
import com.magicapp.exception.domain.TournamentNotFoundException;
import com.magicapp.service.GuestService;
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
    private GuestService guestService;

    private TournamentService tournamentService;

    @Autowired
    public TournamentResource(TournamentService tournamentService, UserService userService, GuestService guestService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
        this.guestService = guestService;
    }

    @GetMapping("/add")
    public ResponseEntity<Tournament> addNewTournament(){
        User currentUser = getCurrentUser();
        Tournament tournament = tournamentService.addNewTournament(currentUser);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}")
    public ResponseEntity<Tournament> getTournament(@PathVariable("tournamentString") String tournamentString) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateUserInTournament(tournament, currentUser);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/guest")
    public ResponseEntity<Tournament> addGuestToTournament(@PathVariable("tournamentString") String tournamentString,
                                                           @RequestParam(value = "firstName", required = false) String firstName,
                                                           @RequestParam(value = "lastName", required = false) String lastName,
                                                           @RequestParam(value = "username") String username) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        Guest guest = guestService.addNewGuest(firstName,lastName,username);
        tournamentService.addPlayer(tournament,guest);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/start")
    public ResponseEntity<Tournament> startTournament(@PathVariable("tournamentString") String tournamentString,
                                                      @RequestParam(value = "rounds") int rounds) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.startTournament(tournament, rounds);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}/nextRound")
    public ResponseEntity<Tournament> nextRoundTournament(@PathVariable("tournamentString") String tournamentString) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.pairNextRoundInTournament(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/gameResults")
    public ResponseEntity<Tournament> addGameResults(@PathVariable("tournamentString") String tournamentString,
                                                     @RequestParam("gamesWonPlayer1") int gamesWonPlayer1,
                                                     @RequestParam("gamesWonPlayer2") int gamesWonPlayer2,
                                                     @RequestParam("gameId") int gameId) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.addGameResults(tournament, gameId, gamesWonPlayer1, gamesWonPlayer2);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }



    @PostMapping("/{tournamentString}/update")
    public ResponseEntity<Tournament> updateTournament(@PathVariable("tournamentString") String tournamentString,
                                                 @RequestParam("userId") Long userId) throws TournamentNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User user = userService.findUserByUserId(userId);
        PlayerParticipation participation = tournament.createParticipationForPlayer(user);
        Guest guest = guestService.addNewGuest("mati", "", "");
        tournament.createParticipationForPlayer(guest);
        RoundMatching roundMatching = new RoundMatching(1);
        tournament.addRoundMatching(roundMatching);
        roundMatching.addGame(new Game(1,participation, participation));

////        Game game = new Game(2L, user, user);
//        tournament.addGame(game);
        tournamentService.saveTournament(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }
    private User getCurrentUser() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByUsername(currentUserName);
        return currentUser;
    }

}
