package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.magicapp.enumeration.GameResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static com.magicapp.enumeration.GameResult.NO_RESULT;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long gameId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournamentId")
    @JsonIgnore
    private Tournament tournament;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roundMatchingId")
    @JsonIgnore
    private RoundMatching roundMatching;
    private int round;
    private int scorePlayer1;
    private int scorePlayer2;
    @Enumerated(EnumType.STRING)
    private GameResult gameResult;
    @ManyToOne
    @JoinColumn(name = "player_1_id")
    private PlayerParticipation player1;
    @ManyToOne
    @JoinColumn(name = "player_2_id")
    private PlayerParticipation player2;


//    public Game(Long gameId, Set<User> users) {
//        this.gameId = gameId;
//        this.users = users;
//    }


    public Game(int round, PlayerParticipation player1, PlayerParticipation player2) {
        this.round = round;
        this.player1 = player1;
        this.player2 = player2;
        this.gameResult = NO_RESULT;
    }

    public boolean hasResult() {
        return gameResult != NO_RESULT;
    }

    public boolean hasPlayerParticipation(PlayerParticipation player) {
        return (player.equals(player1)) || (player.equals(player2));
    }

    public boolean hasPlayerParticipations(PlayerParticipation player1, PlayerParticipation player2) {
        return ((player1.equals(this.player1)) && (player2.equals(this.player2))) ||
                ((player1.equals(this.player2)) && (player2.equals(this.player1)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Game)) {
            return false;
        }
        Game otherGame = (Game) other;
        return ((otherGame.round == round) &&
                (otherGame.player1.equals(player1)) &&
                (otherGame.player2.equals(player2)));
    }
}
