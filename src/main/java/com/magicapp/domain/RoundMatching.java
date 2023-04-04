package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoundMatching {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long roundMatchingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournamentId")
    @JsonIgnore
    private Tournament tournament;
    private int round;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="roundMatching", cascade = CascadeType.ALL)
    private List<Game> games = new ArrayList<>();

    public RoundMatching(int round) {
        this.round = round;
    }

    public void addGame(Game game) {
        for (Game existingGame : games) {
            if (existingGame.getPlayer1().equals(game.getPlayer1())) {
                throw new IllegalArgumentException("Could not add match " + game.getPlayer1().getPlayer().getUsername() + " - " + game.getPlayer2().getPlayer().getUsername() + " : player 1 already matches");
            }
            if (existingGame.getPlayer1().equals(game.getPlayer2())) {
                throw new IllegalArgumentException("Could not add match " + game.getPlayer1().getPlayer().getUsername() + " - " + game.getPlayer2().getPlayer().getUsername() + " : player 2 already matches");
            }
        }
        this.games.add(game);
        game.setRoundMatching(this);
        game.setTournament(tournament);
    }

    boolean hasGameForPlayerParticipation(PlayerParticipation player) {
        for (Game game : games) {
            if (game.hasPlayerParticipation(player)) {
                return true;
            }
        }
        return false;
    }

    boolean removeGameWithPlayerParticipation(PlayerParticipation player) {
        Game foundGame = null;
        for (Game game : games) {
            if (game.hasPlayerParticipation(player)) {
                foundGame = game;
                break;
            }
        }
        if (foundGame != null) {
            return games.remove(foundGame);
        }
        return false;
    }

    public boolean hasAllResults() {
        for (Game game : games) {
            if (!game.hasResult()) {
                return false;
            }
        }
        return true;
    }
}
