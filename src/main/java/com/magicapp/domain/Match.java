package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.magicapp.enumeration.MatchResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static com.magicapp.enumeration.MatchResult.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "\"match\"")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long matchId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournamentId")
    @JsonIgnore
    private Tournament tournament;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roundMatchingId")
    @JsonIgnore
    private RoundMatching roundMatching;
    private int round;
    private int gamesWonPlayer1;
    private int gamesWonPlayer2;
    @Enumerated(EnumType.STRING)
    private MatchResult matchResult;
    @ManyToOne
    @JoinColumn(name = "player_1_id")
    private PlayerParticipation player1;
    @ManyToOne
    @JoinColumn(name = "player_2_id")
    private PlayerParticipation player2;


//    public Match(Long gameId, Set<User> users) {
//        this.gameId = gameId;
//        this.users = users;
//    }


    public Match(int round, PlayerParticipation player1, PlayerParticipation player2) {
        this.round = round;
        this.gamesWonPlayer1 = 0;
        this.gamesWonPlayer2 = 0;
        this.player1 = player1;
        this.player2 = player2;
        this.matchResult = NO_RESULT;
    }

    public boolean hasResult() {
        return matchResult != NO_RESULT;
    }

    public boolean hasPlayerParticipation(PlayerParticipation player) {
        return (player.equals(player1)) || (player.equals(player2));
    }

    public boolean hasPlayerParticipations(PlayerParticipation player1, PlayerParticipation player2) {
        return ((player1.equals(this.player1)) && (player2.equals(this.player2))) ||
                ((player1.equals(this.player2)) && (player2.equals(this.player1)));
    }

    public PlayerParticipation getOpponent(PlayerParticipation player) {
        if(player.equals(this.player1)){
            return player2;
        }
        if(player.equals(this.player2)){
            return player1;
        }
        return null;
    }

    public void calculateResult(){
        if(gamesWonPlayer1 > gamesWonPlayer2){
            this.setMatchResult(PLAYER_1_WON);
        }
        if(gamesWonPlayer1 < gamesWonPlayer2){
            this.setMatchResult(PLAYER_2_WON);
        }
        if(gamesWonPlayer1 == gamesWonPlayer2){
            this.setMatchResult(DRAW);
        }
    }

    public void setPlayerScores(){
        player1.addToGamesWon(gamesWonPlayer1);
        player2.addToGamesWon(gamesWonPlayer2);
        player1.addToGamesLost(gamesWonPlayer2);
        player2.addToGamesLost(gamesWonPlayer1);

        if(matchResult == PLAYER_1_WON){
            player1.addToScore(3);
            player1.addToMatchesWon(1);
            player2.addToMatchesLost(1);
        }
        if(matchResult == PLAYER_2_WON){
            player2.addToScore(3);
            player2.addToMatchesWon(1);
            player1.addToMatchesLost(1);
        }
        if(matchResult == DRAW){
            player1.addToScore(1);
            player2.addToScore(1);
            player1.addToMatchesTied(1);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Match)) {
            return false;
        }
        Match otherMatch = (Match) other;
        return ((otherMatch.round == round) &&
                (otherMatch.player1.equals(player1)) &&
                (otherMatch.player2.equals(player2)));
    }
}
