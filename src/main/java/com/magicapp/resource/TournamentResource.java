package com.magicapp.resource;

import com.magicapp.domain.Game;
import com.magicapp.domain.Tournament;
import com.magicapp.domain.User;
import com.magicapp.repository.TournamentRepository;
import com.magicapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "*")
@RequestMapping(path = { "/tournament"})
public class TournamentResource {

    private  TournamentRepository tournamentRepository;

    private UserRepository userRepository;

    @Autowired
    public TournamentResource(TournamentRepository tournamentRepository, UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{tournamentString}")
    public ResponseEntity<Tournament> addNewTournament(@PathVariable("tournamentString") String tournamentString){
        Tournament tournament = new Tournament();
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByUsername(currentUserName);
        tournament.addUser(currentUser);
        tournament.setTournamentString(tournamentString);
        tournamentRepository.save(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }

    @GetMapping("/{tournamentString}")
    public ResponseEntity<Tournament> getTournament(@PathVariable("tournamentString") String tournamentString){
        Tournament getTournament = tournamentRepository.findByTournamentString(tournamentString);

        return new ResponseEntity<>(getTournament, HttpStatus.OK);
    }

    @PostMapping("/{tournamentString}/update")
    public ResponseEntity<Tournament> updateTournament(@PathVariable("tournamentString") String tournamentString,
                                                 @RequestParam("userId") Long userId){
        Tournament tournament = tournamentRepository.findByTournamentString(tournamentString);
        User user = userRepository.findUserByUserId(userId);
        tournament.addUser(user);
////        Game game = new Game(2L, user, user);
//        tournament.addGame(game);
        tournamentRepository.save(tournament);
        return new ResponseEntity<>(tournament, HttpStatus.OK);
    }
}
