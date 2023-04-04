package com.magicapp.domain;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

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

    public PlayerParticipation createParticipationForPlayer(Player player) {
        PlayerParticipation participation = new PlayerParticipation(this, player);
        this.participations.add(participation);
//        player.getParticipations().add(participation);
        return participation;
    }

    public void addGame(Game game) {
        this.allGames.add(game);
        game.setTournament(this);
    }

    public void addRoundMatching(RoundMatching roundMatching) {
        this.roundMatchings.add(roundMatching);
        roundMatching.setTournament(this);
    }


    public synchronized RoundMatching pairNextRound() {
        currentRound++;
        if (currentRound > rounds) {
            throw new IllegalArgumentException("Tournament ended after " + rounds + " rounds");
        }
        if (currentRound == 1) {
            return firstRoundRandomMatching();
        }

        // not first round. check that previous round has all results
        RoundMatching lastRoundMatching = roundMatchings.get(currentRound - 2);
        if (!lastRoundMatching.hasAllResults()) {
            throw new IllegalStateException("Round " + (currentRound - 1) + " results were not set yet");
        }

        // pair new round
        RoundMatching matching = getNextRoundMatching();
        roundMatchings.add(matching);
        return matching;
    }

    private RoundMatching getNextRoundMatching() {
        // sort the players based on their score
        List<PlayerParticipation> sortedPlayers = new ArrayList<PlayerParticipation>(participations);
        Collections.sort(sortedPlayers);
        Long byePlayerId = -1L;
        int numberOfPlayers = participations.size();
        RoundMatching newMatching = new RoundMatching(currentRound);
        newMatching.setTournament(this);

        if ((numberOfPlayers % 2) == 1) {
            // choose a bye player
            int index = 0;
            while (index <= numberOfPlayers) {
                PlayerParticipation player = sortedPlayers.get(index);
                if (player.getByeRound() == 0) {
                    player.setByeRound(currentRound);
                    byePlayerId = player.getId(); // -------------------------------------------------------id?
                    LOGGER.fine("player " + player + " bye round " + currentRound);
                    break;
                }
                index++;
            }
        }

        // iterate over the players, find a matching for the top player
        for (int i = 0; i < numberOfPlayers; i++) {

            PlayerParticipation bestScorePlayer;

            bestScorePlayer = sortedPlayers.get(i);

            if (bestScorePlayer.getId() == byePlayerId) {
                LOGGER.fine("player " + bestScorePlayer + " bye this round");
                continue;
            }

            // check if this player is already scheduled this round
            if (newMatching.hasGameForPlayerParticipation(bestScorePlayer)) {
                LOGGER.fine("round " + currentRound + " player " + bestScorePlayer + " already scheduled");
                continue;
            }

            boolean matchForBestPlayerFound = false;
            for (int j = i + 1; j < numberOfPlayers; j++) {

                PlayerParticipation nextScorePlayer;
                nextScorePlayer = participations.get(j);

                if (nextScorePlayer.getId() == byePlayerId) {
                    LOGGER.fine(nextScorePlayer + " bye this round");
                    continue;
                }

                // check if this player is already scheduled this round
                if (newMatching.hasGameForPlayerParticipation(nextScorePlayer)) {
                    LOGGER.fine("round " + currentRound + " player " + nextScorePlayer + " already scheduled");
                    continue;
                }

                // check if such game already happened
                if (listContainsMatchBetweenPlayers(allGames, bestScorePlayer, nextScorePlayer)) {
                    // already played. find next opponent
                    LOGGER.fine("round " + currentRound + " game " + bestScorePlayer + " - " + nextScorePlayer + " exists");
                    continue;
                }

                Game game = new Game(currentRound, bestScorePlayer, nextScorePlayer);
                allGames.add(game);
                newMatching.addGame(game);
                matchForBestPlayerFound = true;
                break;
            }

            if (matchForBestPlayerFound) {
                // ok ! continue on to next player
                continue;
            }

            if (newMatching.getGames().size() == (numberOfPlayers / 2)) {
                // we have all games that we need
                continue;
            }

            // no match for the best player found. we now have to find a couple to break,
            // and opp for this player that will satisfy all conditions
            // so iterate on the pairing so far in reverse order
            LOGGER.fine("round " + currentRound + " need to switch pairs for " + bestScorePlayer + " we have " + newMatching.getGames().size() + " games");

            for (int g = newMatching.getGames().size() - 1; g >= 0; g--) {
                Game pairedGame = newMatching.getGames().get(g);
                // see if the best player can be matched vs any of this couple
                PlayerParticipation player1 = pairedGame.getPlayer1();
                PlayerParticipation player2 = pairedGame.getPlayer2();

                if ((listContainsMatchBetweenPlayers(allGames, bestScorePlayer, player1)) &&
                        (listContainsMatchBetweenPlayers(allGames, bestScorePlayer, player2))) {
                    // we can't use this pair because the best score user already played vs both of them
                    continue;
                }

                // ok have a candidate pairing. lets iterate over the players again from the bottom to find someone
                // to switch pairs with

                PlayerParticipation switchPlayer;

                for (int p = numberOfPlayers - 1; p >= 0; p--) {

                    switchPlayer = participations.get(p);

                    // check that the switch player is not scheduled, and that it is not the bye user, or the best
                    // score user, or the chosen pairs wid,bid
                    if (newMatching.hasGameForPlayerParticipation(switchPlayer)) {
                        LOGGER.fine("round " + currentRound + " switch user " + switchPlayer + " already scheduled");
                        continue;
                    }

                    if ((switchPlayer.equals(bestScorePlayer)) || (switchPlayer.getId() == byePlayerId) ||
                            (switchPlayer.equals(player1)) || (switchPlayer.equals(player2))) {
                        LOGGER.fine("round " + currentRound + " switch user " + switchPlayer + " is either the best score, bye, wid or bid");
                        continue;
                    }

                    LOGGER.fine("round " + currentRound + " candidate switch player " + switchPlayer);

                    // ok ! the last thing to check it that it is possible to make some pairing switch

                    if (!((listContainsMatchBetweenPlayers(allGames, player1, switchPlayer)) ||
                            (listContainsMatchBetweenPlayers(allGames, player2, bestScorePlayer)))) {
                        // we can switch. wid vs the switch user, best player vs bid
                        LOGGER.fine("pairing remove game " + pairedGame);

                        if (!newMatching.removeGameWithPlayerParticipation(player1)) {
                            LOGGER.warning("could not remove game with " + player1);
                            return null;
                        }

                        Game game = new Game(currentRound, player1, switchPlayer);
                        allGames.add(game);
                        newMatching.addGame(game);
                        Game game2 = new Game(currentRound, player2, bestScorePlayer);
                        allGames.add(game2);
                        newMatching.addGame(game2);

                        matchForBestPlayerFound = true;
                        break;
                    }

                    if (!((listContainsMatchBetweenPlayers(allGames, player2, switchPlayer)) ||
                            (listContainsMatchBetweenPlayers(allGames, player1, bestScorePlayer)))) {
                        // we can switch. wid vs the switch user, best player vs bid
                        LOGGER.fine("pairing remove game " + pairedGame);

                        if (!newMatching.removeGameWithPlayerParticipation(player1)) {
                            LOGGER.warning("could not remove match with " + player1);
                            return null;
                        }

                        Game game = new Game(currentRound, player2, switchPlayer);
                        allGames.add(game);
                        newMatching.addGame(game);
                        Game game2 = new Game(currentRound, player1, bestScorePlayer);
                        allGames.add(game2);
                        newMatching.addGame(game2);

                        matchForBestPlayerFound = true;
                        break;
                    }
                }

                if (matchForBestPlayerFound) {
                    break;
                }
            }

            if (!matchForBestPlayerFound) {
                // nothing to do... probably not enough players or some crazy pairing
                LOGGER.warning("could not match all players. not enough players ?");
                break;
            }
        }

        return newMatching;
    }

    private static boolean listContainsMatchBetweenPlayers(List<Game> games, PlayerParticipation player1, PlayerParticipation player2) {
        for (Game game : games) {
            if (game.hasPlayerParticipations(player1, player2)) {
                return true;
            }
        }
        return false;
    }

    public RoundMatching firstRoundRandomMatching()
    {
        Random random = new Random();
        RoundMatching matching = new RoundMatching(currentRound);
        matching.setTournament(this);
        List<PlayerParticipation> notPairedYet = new ArrayList<PlayerParticipation>(participations);
        while (notPairedYet.size() > 1) {
            int player1Index = random.nextInt(notPairedYet.size());
            PlayerParticipation player1 = notPairedYet.get(player1Index);
            notPairedYet.remove(player1Index);
            int player2Index = random.nextInt(notPairedYet.size());
            PlayerParticipation player2 = notPairedYet.get(player2Index);
            notPairedYet.remove(player2Index);
            Game game = new Game(currentRound, player1, player2);
            allGames.add(game);
            matching.addGame(game);
        }
        // set bye round for player without a pair
        if(notPairedYet.size() == 1){
            PlayerParticipation player = notPairedYet.get(0);
            player.setByeRound(currentRound);
        }
        roundMatchings.add(matching);
        return matching;
    }
}
