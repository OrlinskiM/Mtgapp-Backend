package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlayerParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "tournament_id")
    @JsonIgnore
    private Tournament tournament;
    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "user_id")
    private Player player;

    private int score;
    private int roundsWon;
    private int gamesWon;
    private int byeRound;

    public PlayerParticipation(Tournament tournament, Player player) {
        this.tournament = tournament;
        this.player = player;
    }
}
