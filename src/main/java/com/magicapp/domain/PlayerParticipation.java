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
public class PlayerParticipation implements Comparable<PlayerParticipation>{
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
    private int finalPlacement;
    private int score;  //matchesWon * 3 + matchesTied + byeRound * 3
    private int matchesWon;
    private int matchesLost;
    private int matchesTied;
    private int gamesWon;
    private int gamesLost;
    private int byeRound;
    private float omw; //Opponents’ match-win percentage
    private float gw; //Game-win percentage
    private float ogw; //Opponents’ game-win percentage

    public PlayerParticipation(Tournament tournament, Player player) {
        this.tournament = tournament;
        this.player = player;
    }

    public void addToScore(int value){
        score += value;
    }

    public void addToMatchesWon(int value){
        matchesWon += value;
    }

    public void addToMatchesLost(int value){
        matchesLost += value;
    }

    public void addToMatchesTied(int value){
        matchesTied += value;
    }

    public void addToGamesWon(int value){
        gamesWon += value;
    }

    public void addToGamesLost(int value){
        gamesLost += value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof PlayerParticipation)) {
            return false;
        }
        PlayerParticipation otherPlayer = (PlayerParticipation) other;
        return otherPlayer.id.equals(id);
    }
    @Override
    public int compareTo(PlayerParticipation other) {
        if (score > other.score) {
            return 1;
        }
        if (score < other.score) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString(){
        return player.getUsername();
    }
}
