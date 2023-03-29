package com.magicapp.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "guest")
public class Guest extends Player{
    public Guest(String firstName, String lastName, String username, String profileImageUrl) {
        super(firstName,lastName, username, profileImageUrl);
    }

}
