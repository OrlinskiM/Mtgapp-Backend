package com.magicapp.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlayerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long playerScoreId;

    private int roundsWon;
    private int gamesWon;
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
}
