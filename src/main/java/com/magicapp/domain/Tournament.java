package com.magicapp.domain;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tournament")
public class Tournament implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
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
    @ManyToMany(
            cascade = {CascadeType.ALL}
    )
    @JoinTable(
            name = "player_tournament",
            joinColumns = @JoinColumn(name = "tournamentId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private Set<Player> players = new HashSet<>();
    @OneToMany(mappedBy="tournament", cascade = CascadeType.ALL)
    private Set<Game> allGames = new HashSet<>();
    @OneToMany(mappedBy="tournament", cascade = CascadeType.ALL)
    private Set<RoundMatching> roundMatchings = new HashSet<>();



    public Tournament(Long tournamentId, String tournamentString, boolean isFinished, Date creationDate, Date finishDate) {
        this.tournamentId = tournamentId;
//        this.ownerId = ownerId;
        this.tournamentString = tournamentString;
        this.isFinished = isFinished;
        this.creationDate = creationDate;
        this.finishDate = finishDate;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        player.getTournaments().add(this);
    }

    public void addGame(Game game) {
        this.allGames.add(game);
        game.setTournament(this);
    }

    public void addRoundMatching(RoundMatching roundMatching) {
        this.roundMatchings.add(roundMatching);
        roundMatching.setTournament(this);
    }

    public void removeUser(User user) {
        this.players.remove(user);
        user.getTournaments().remove(this);
    }

//    public void addGame(Game game) {
//        this.allGames.add(game);
//        game.setTournament(this);
//    }
//
//    public void removeGame(Game game) {
//        this.allGames.remove(game);
//    }

}
