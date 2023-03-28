package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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
//    @ManyToMany(
//            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
//    )
//    @JoinTable(
//            name = "user_game",
//            joinColumns = @JoinColumn(name = "gameId"),
//            inverseJoinColumns = @JoinColumn(name = "userId")
//    )
//    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "player_1_id")
    private Player player1;
    @ManyToOne
    @JoinColumn(name = "player_2_id")
    private Player player2;


//    public Game(Long gameId, Set<User> users) {
//        this.gameId = gameId;
//        this.users = users;
//    }


    public Game(int round, Player player1, Player player2) {
        this.round = round;
        this.player1 = player1;
        this.player2 = player2;
    }
}
