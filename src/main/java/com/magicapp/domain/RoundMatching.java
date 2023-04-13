package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    private List<Match> matches = new ArrayList<>();

    public RoundMatching(int round) {
        this.round = round;
    }

    public void addMatch(Match match) {
        for (Match existingMatch : matches) {
            if (existingMatch.getPlayer1().equals(match.getPlayer1())) {
                throw new IllegalArgumentException("Could not add match " + match.getPlayer1().getPlayer().getUsername() + " - " + match.getPlayer2().getPlayer().getUsername() + " : player 1 already matches");
            }
            if (existingMatch.getPlayer1().equals(match.getPlayer2())) {
                throw new IllegalArgumentException("Could not add match " + match.getPlayer1().getPlayer().getUsername() + " - " + match.getPlayer2().getPlayer().getUsername() + " : player 2 already matches");
            }
        }
        this.matches.add(match);
        match.setRoundMatching(this);
        match.setTournament(tournament);
    }

    boolean hasMatchForPlayerParticipation(PlayerParticipation player) {
        for (Match match : matches) {
            if (match.hasPlayerParticipation(player)) {
                return true;
            }
        }
        return false;
    }

    boolean removeMatchWithPlayerParticipation(PlayerParticipation player) {
        Match foundMatch = null;
        for (Match match : matches) {
            if (match.hasPlayerParticipation(player)) {
                foundMatch = match;
                break;
            }
        }
        if (foundMatch != null) {
            return matches.remove(foundMatch);
        }
        return false;
    }

    public boolean hasAllResults() {
        for (Match match : matches) {
            if (!match.hasResult()) {
                return false;
            }
        }
        return true;
    }
}
