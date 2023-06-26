package com.magicapp.resource;

import com.magicapp.domain.*;
import com.magicapp.exception.domain.TournamentNotFoundException;
import com.magicapp.exception.domain.UserNotFoundException;
import com.magicapp.service.GuestService;
import com.magicapp.service.TournamentService;
import com.magicapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Tournament> addNewTournament() throws UserNotFoundException {
        User currentUser = getCurrentUser();
        Tournament tournament = tournamentService.addNewTournament(currentUser);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}")
    public ResponseEntity<Tournament> getTournament(@PathVariable("tournamentString") String tournamentString) throws TournamentNotFoundException, UserNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateUserInTournament(tournament, currentUser);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Tournament[]> findAllUsersTournaments() throws UserNotFoundException {
        User currentUser = getCurrentUser();
        Tournament[] tournaments = tournamentService.findAllByUserId(currentUser.getUserId());
        return new ResponseEntity<>(tournaments, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/guest")
    public ResponseEntity<Tournament> addGuestToTournament(@PathVariable("tournamentString") String tournamentString,
                                                           @RequestParam(value = "firstName", required = false) String firstName,
                                                           @RequestParam(value = "lastName", required = false) String lastName,
                                                           @RequestParam(value = "username") String username) throws TournamentNotFoundException, UserNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        Guest guest = guestService.addNewGuest(firstName,lastName,username);
        tournamentService.addPlayer(tournament,guest);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/start")
    public ResponseEntity<Tournament> startTournament(@PathVariable("tournamentString") String tournamentString,
                                                      @RequestParam(value = "rounds") int rounds) throws TournamentNotFoundException, UserNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.startTournament(tournament, rounds);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}/nextRound")
    public ResponseEntity<Tournament> nextRoundTournament(@PathVariable("tournamentString") String tournamentString) throws TournamentNotFoundException, UserNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.pairNextRoundInTournament(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/matchResults")
    public ResponseEntity<Tournament> addMatchResults(@PathVariable("tournamentString") String tournamentString,
                                                     @RequestParam("gamesWonPlayer1") int gamesWonPlayer1,
                                                     @RequestParam("gamesWonPlayer2") int gamesWonPlayer2,
                                                     @RequestParam("matchId") int matchId) throws TournamentNotFoundException, UserNotFoundException {
        Tournament tournament = tournamentService.findByTournamentString(tournamentString);
        User currentUser = getCurrentUser();
        tournamentService.validateOwnerInTournament(tournament, currentUser);
        tournamentService.addMatchResults(tournament, matchId, gamesWonPlayer1, gamesWonPlayer2);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }


    private User getCurrentUser() throws UserNotFoundException {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByUsername(currentUserName);
        return currentUser;
    }

}
