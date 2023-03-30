package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import lombok.AccessLevel;
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
    private Set<Game> games = new HashSet<>();

    public RoundMatching(int round) {
        this.round = round;
    }

    public void addGame(Game game) {
        this.games.add(game);
        game.setRoundMatching(this);
        game.setTournament(tournament);
    }

}
