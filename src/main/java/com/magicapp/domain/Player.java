package com.magicapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Player{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String profileImageUrl;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<PlayerParticipation> participations = new HashSet<>();

    public Player(String firstName, String lastName, String username, String profileImageUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }
}
