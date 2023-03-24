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
@Getter @Setter @NoArgsConstructor
@Table(name = "tournament")
public class Tournament implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long tournamentId;
    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    @JoinTable(
            name = "user_tournament",
            joinColumns = @JoinColumn(name = "tournamentId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private Set<User> users = new HashSet<>();
    private String tournamentString;
    private int rounds;
    private int currentRound;
    @OneToMany(mappedBy="tournament", cascade = CascadeType.ALL)
    private Set<Game> games = new HashSet<>();
    private boolean isFinished;
    private Date creationDate;
    private Date finishDate;



    public Tournament(Long tournamentId, User ownerId, String tournamentString, boolean isFinished, Date creationDate, Date finishDate) {
        this.tournamentId = tournamentId;
//        this.ownerId = ownerId;
        this.tournamentString = tournamentString;
        this.isFinished = isFinished;
        this.creationDate = creationDate;
        this.finishDate = finishDate;
    }

    public void addUser(User user) {
        this.users.add(user);
        user.getTournaments().add(this);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        user.getTournaments().remove(this);
    }

    public void addGame(Game game) {
        this.games.add(game);
        game.setTournament(this);
    }

    public void removeGame(Game game) {
        this.games.remove(game);
    }

}
