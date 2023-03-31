package com.magicapp.domain;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tournament")
public class Tournament implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long tournamentId;
    private String tournamentString;
    private int rounds;
    private int currentRound;
    private boolean isFinished;
    private Date creationDate;
    private Date finishDate;
    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private User owner;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<PlayerParticipation> participations = new ArrayList<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy="tournament", cascade = CascadeType.ALL)
    private List<Game> allGames = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, mappedBy="tournament", cascade = CascadeType.ALL)
    private List<RoundMatching> roundMatchings = new ArrayList<>();



    public Tournament(Long tournamentId, String tournamentString, boolean isFinished, Date creationDate, Date finishDate) {
        this.tournamentId = tournamentId;
//        this.ownerId = ownerId;
        this.tournamentString = tournamentString;
        this.isFinished = isFinished;
        this.creationDate = creationDate;
        this.finishDate = finishDate;
    }

    public void addPlayer(Player player) {
        PlayerParticipation participation = new PlayerParticipation(this, player);
        this.participations.add(participation);
        player.getParticipations().add(participation);
    }

    public void addGame(Game game) {
        this.allGames.add(game);
        game.setTournament(this);
    }

    public void addRoundMatching(RoundMatching roundMatching) {
        this.roundMatchings.add(roundMatching);
        roundMatching.setTournament(this);
    }


//    public void removeUser(User user) {
//        this.players.remove(user);
//        user.getTournaments().remove(this);
//    }

//    public void addGame(Game game) {
//        this.allGames.add(game);
//        game.setTournament(this);
//    }
//
//    public void removeGame(Game game) {
//        this.allGames.remove(game);
//    }

}
