package com.magicapp.domain;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ComparisonChain;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    @OneToMany(fetch = FetchType.LAZY, mappedBy="tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> allMatches = new ArrayList<>();
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

    public void addMatch(Match match) {
        this.allMatches.add(match);
        match.setTournament(this);
    }

    public void addRoundMatching(RoundMatching roundMatching) {
        this.roundMatchings.add(roundMatching);
        roundMatching.setTournament(this);
    }


    public synchronized RoundMatching pairNextRound() {
        currentRound++;

        if (isFinished) {
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

        calculatePlayerScores(lastRoundMatching);

        if (currentRound == rounds+1) {
            this.currentRound = rounds;
            this.finishDate = new Date();
            this.isFinished = true;
            calculateFinalPlacement();
            return lastRoundMatching;
        }

        // pair new round
        RoundMatching matching = getNextRoundMatching();
        roundMatchings.add(matching);
        return matching;
    }

    private void calculateFinalPlacement() {
        List<PlayerParticipation> sortedPlayers = new ArrayList<PlayerParticipation>(participations);
        Collections.sort(sortedPlayers, new Comparator<PlayerParticipation>() {
            @Override
            public int compare(PlayerParticipation o1, PlayerParticipation o2) {
                return ComparisonChain.start()
                        .compare(o1.getScore(), o2.getScore())
                        .compare(o1.getOmw(), o2.getOmw())
                        .compare(o1.getGw(), o2.getGw())
                        .compare(o1.getOgw(), o2.getOgw())
                        .result();
            }
        });
        int index = sortedPlayers.size();
        for(PlayerParticipation player: sortedPlayers){
            player.setFinalPlacement(index);
            index--;
        }
    }

    private void calculatePlayerScores(RoundMatching lastRoundMatching) {
        //first, set all scores from previous round
        for(Match match: lastRoundMatching.getMatches()){
            match.setPlayerScores();
        }

        //second, calculate tiebreakers
        for(PlayerParticipation player: participations){
            //add a win for bye player
            if(player.getByeRound() == currentRound-1){
                player.addToScore(3);
                player.addToGamesWon(2);
            }

            setPlayerOmw(player);
            setPlayerGw(player);
            setPlayerOgw(player);

        }


    }

    private boolean setPlayerGw(PlayerParticipation player) {
        //calculate players game win % (gw)
        float gw = 0;
        //game points divided by total number of games played * 3
        float gameScore = player.getGamesWon() * 3;
        float maxPossibleGameScore = (player.getGamesWon() + player.getGamesLost()) * 3;

        gw = Math.round((gameScore/maxPossibleGameScore)*100f)/100f;
        player.setGw(gw);

        return true;
    }

    private boolean setPlayerOmw(PlayerParticipation player){
        //calculate players opponent match win % (omw)
        float omw = 0;
        int maxPossibleScore = (currentRound-1)*3;
        List<PlayerParticipation> opponents = getPlayersOpponentsList(player);
        //add all opponents percentages
        for(PlayerParticipation opponent: opponents){
            int opponentsScore = opponent.getScore();
            float opponentPercent = ((float)opponentsScore)/maxPossibleScore;
            if(opponentPercent > 0.33){
                omw += opponentPercent;
            } else{
                omw += 0.33f;
            }

        }
        //divide by number of opponents
        omw = Math.round(omw/((float)opponents.size())*100f)/100f;
        //omw can't be smaller than 0.33
        if(omw < 0.33){
            omw = 0.33f;
        }
        player.setOmw(omw);
        return true;
    }

    private boolean setPlayerOgw(PlayerParticipation player){
        //calculate players opponent game win % (ogw)
        float ogw = 0;
        List<PlayerParticipation> opponents = getPlayersOpponentsList(player);
        //add all opponents percentages
        for(PlayerParticipation opponent: opponents){
            int opponentsGameScore = opponent.getGamesWon() * 3;
            int maxPossibleGameScore = (opponent.getGamesWon() + opponent.getGamesLost())*3;
            float opponentPercent = ((float)opponentsGameScore)/maxPossibleGameScore;
            if(opponentPercent > 0.33){
                ogw += opponentPercent;
            } else{
                ogw += 0.33f;
            }

        }
        //divide by number of opponents
        ogw = Math.round(ogw/((float)opponents.size())*100f)/100f;
        //omw can't be smaller than 0.33
        if(ogw < 0.33){
            ogw = 0.33f;
        }
        player.setOgw(ogw);
        return true;
    }

    private List<PlayerParticipation> getPlayersOpponentsList(PlayerParticipation player) {
        List<PlayerParticipation> opponents = new ArrayList<>();
        for(Match match: allMatches){
            PlayerParticipation opponent = match.getOpponent(player);
            if(opponent != null){
                opponents.add(opponent);
            }
        }
        return opponents;
    }

    private RoundMatching getNextRoundMatching() {
        Logger LOGGER = LoggerFactory.getLogger(getClass());
        // sort the players based on their score
        List<Match> allMatchesTemp = allMatches;
        List<PlayerParticipation> sortedPlayers = new ArrayList<PlayerParticipation>(participations);
        Collections.sort(sortedPlayers, Collections.reverseOrder());
        Long byePlayerId = -1L;
        int numberOfPlayers = participations.size();
        RoundMatching newMatching = new RoundMatching(currentRound);
        newMatching.setTournament(this);

        if ((numberOfPlayers % 2) == 1) {
            // choose a bye player
            int index = numberOfPlayers-1;
            while (index >= 0) {
                PlayerParticipation player = sortedPlayers.get(index);
                if (player.getByeRound() == 0) {
                    player.setByeRound(currentRound);
                    byePlayerId = player.getId(); // -------------------------------------------------------id?
                    LOGGER.info("player " + player + " bye round " + currentRound);
                    break;
                }
                index--;
            }
        }

        // iterate over the players, find a matching for the top player
        for (int i = 0; i < numberOfPlayers; i++) {

            PlayerParticipation bestScorePlayer;

            bestScorePlayer = sortedPlayers.get(i);

            if (bestScorePlayer.getId() == byePlayerId) {
                LOGGER.info("player " + bestScorePlayer + " bye this round");
                continue;
            }

            // check if this player is already scheduled this round
            if (newMatching.hasMatchForPlayerParticipation(bestScorePlayer)) {
                LOGGER.info("round " + currentRound + " player " + bestScorePlayer + " already scheduled");
                continue;
            }

            boolean matchForBestPlayerFound = false;
            for (int j = i + 1; j < numberOfPlayers; j++) {

                PlayerParticipation nextScorePlayer;
                nextScorePlayer = sortedPlayers.get(j);

                if (nextScorePlayer.getId() == byePlayerId) {
                    LOGGER.info(nextScorePlayer + " bye this round");
                    continue;
                }

                // check if this player is already scheduled this round
                if (newMatching.hasMatchForPlayerParticipation(nextScorePlayer)) {
                    LOGGER.info("round " + currentRound + " player " + nextScorePlayer + " already scheduled");
                    continue;
                }

                // check if such match already happened
                if (listContainsMatchBetweenPlayers(allMatches, bestScorePlayer, nextScorePlayer)) {
                    // already played. find next opponent
                    LOGGER.info("round " + currentRound + " match " + bestScorePlayer + " - " + nextScorePlayer + " exists");
                    continue;
                }

                Match match = new Match(currentRound, bestScorePlayer, nextScorePlayer);
                allMatches.add(match);
                newMatching.addMatch(match);
                matchForBestPlayerFound = true;
                LOGGER.info("1matching " + bestScorePlayer.toString() + " and " + nextScorePlayer.toString());
                break;
            }

            if (matchForBestPlayerFound) {
                // ok ! continue on to next player
                continue;
            }

            if (newMatching.getMatches().size() == (numberOfPlayers / 2)) {
                // we have all games that we need
                continue;
            }

            // no match for the best player found. we now have to find a couple to break,
            // and opp for this player that will satisfy all conditions
            // so iterate on the pairing so far in reverse order
            LOGGER.info("round " + currentRound + " need to switch pairs for " + bestScorePlayer + " we have " + newMatching.getMatches().size() + " games");

            for (int g = newMatching.getMatches().size() - 1; g >= 0; g--) {
                Match pairedMatch = newMatching.getMatches().get(g);
                // see if the best player can be matched vs any of this couple
                PlayerParticipation player1 = pairedMatch.getPlayer1();
                PlayerParticipation player2 = pairedMatch.getPlayer2();

                if ((listContainsMatchBetweenPlayers(allMatches, bestScorePlayer, player1)) &&
                        (listContainsMatchBetweenPlayers(allMatches, bestScorePlayer, player2))) {
                    // we can't use this pair because the best score user already played vs both of them
                    continue;
                }

                // ok have a candidate pairing. lets iterate over the players again from the bottom to find someone
                // to switch pairs with

                PlayerParticipation switchPlayer;

                for (int p = numberOfPlayers - 1; p >= 0; p--) {

                    switchPlayer = sortedPlayers.get(p);

                    // check that the switch player is not scheduled, and that it is not the bye user, or the best
                    // score user, or the chosen pairs wid,bid
                    if (newMatching.hasMatchForPlayerParticipation(switchPlayer)) {
                        LOGGER.info("round " + currentRound + " switch user " + switchPlayer + " already scheduled");
                        continue;
                    }

                    if ((switchPlayer.equals(bestScorePlayer)) || (switchPlayer.getId() == byePlayerId) ||
                            (switchPlayer.equals(player1)) || (switchPlayer.equals(player2))) {
                        LOGGER.info("round " + currentRound + " switch user " + switchPlayer + " is either the best score, bye, wid or bid");
                        continue;
                    }

                    LOGGER.info("round " + currentRound + " candidate switch player " + switchPlayer);

                    // ok ! the last thing to check it that it is possible to make some pairing switch

                    if (!((listContainsMatchBetweenPlayers(allMatches, player1, switchPlayer)) ||
                            (listContainsMatchBetweenPlayers(allMatches, player2, bestScorePlayer)))) {
                        // we can switch. wid vs the switch user, best player vs bid
                        LOGGER.info("pairing update match " + pairedMatch.getMatchId());


                        pairedMatch.setPlayer1(player1);
                        pairedMatch.setPlayer2(switchPlayer);
                        LOGGER.info("2matching " + switchPlayer.toString() + " and " + player1.toString());
                        Match match2 = new Match(currentRound, player2, bestScorePlayer);
                        allMatches.add(match2);
                        newMatching.addMatch(match2);
                        LOGGER.info("3matching " + bestScorePlayer.toString() + " and " + player2.toString());

                        matchForBestPlayerFound = true;
                        break;
                    }

                    if (!((listContainsMatchBetweenPlayers(allMatches, player2, switchPlayer)) ||
                            (listContainsMatchBetweenPlayers(allMatches, player1, bestScorePlayer)))) {
                        // we can switch. wid vs the switch user, best player vs bid
                        LOGGER.info("pairing update match " + pairedMatch);


                        pairedMatch.setPlayer1(player2);
                        pairedMatch.setPlayer2(switchPlayer);
                        Match match2 = new Match(currentRound, player1, bestScorePlayer);
                        allMatches.add(match2);
                        newMatching.addMatch(match2);

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
                LOGGER.error("could not match all players. not enough players ?");
                break;
            }
        }

        return newMatching;
    }

    private boolean removeMatchWithPlayerParticipationFromAll(PlayerParticipation player) {
        Match foundMatch = null;
        for (Match match : allMatches) {
            if (match.hasPlayerParticipation(player)) {
                foundMatch = match;
                break;
            }
        }
        if (foundMatch != null) {
            return allMatches.remove(foundMatch);
        }
        return false;
    }


    private static boolean listContainsMatchBetweenPlayers(List<Match> matches, PlayerParticipation player1, PlayerParticipation player2) {
        for (Match match : matches) {
            if (match.hasPlayerParticipations(player1, player2)) {
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
            Match match = new Match(currentRound, player1, player2);
            allMatches.add(match);
            matching.addMatch(match);
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
