package com.memo.game.service;

import com.memo.game.entity.MemoUser;
import com.memo.game.repo.MemoMultiGameRepository;
import com.memo.game.repo.MemoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing and retrieving multiplayer game statistics.
 * Provides functionality to generate leaderboards and other statistical data.
 */
@Service
public class MultiPlayerStatService {
    private final MemoMultiGameRepository memoMultiGameRepository;
    private final MemoUserRepository memoUserRepository;
    @Autowired
    public  MultiPlayerStatService(MemoMultiGameRepository memoMultiGameRepository, MemoUserRepository memoUserRepository) {
        this.memoMultiGameRepository = memoMultiGameRepository;
        this.memoUserRepository = memoUserRepository;
    }

    /**
     * Retrieves a leaderboard of users with their win counts for a specific number of pairs.
     *
     * This method fetches all users from the database, then retrieves and counts their wins for the specified number of pairs.
     * It returns a hashmap where the keys are usernames and the values are their corresponding win counts.
     *
     * @param pairs the number of pairs to filter the win counts
     * @return a hashmap containing usernames and their win counts
     */
    private HashMap<String, Integer> getUsersWithWins(int pairs) {
        HashMap<String, Integer> leaderBoard = new HashMap<>();
        List<MemoUser> users = memoUserRepository.findAll();
        for(MemoUser user : users) {
            Integer wins = memoMultiGameRepository.getWins((user.getId()).toString(), pairs);
            leaderBoard.put(user.getUserName(), wins);
        }
        return leaderBoard;
    }

    /**
     * Creates a ranked leaderboard from a hashmap of usernames and win counts.
     *
     * This method takes a hashmap of usernames and their win counts, and converts it into a ranked leaderboard.
     * It maintains the rank of users based on their win counts. Users with the same win count have the same rank.
     * The result is a list of hashmaps, each containing the username, win count, and rank.
     *
     * @param leaderboard a hashmap of usernames and their win counts
     * @return a list of hashmaps representing the ranked leaderboard
     */
    private List<HashMap<String, Object>> makeRankedLeaderBoard(HashMap<String, Integer> leaderboard) {
        List<HashMap<String, Object>> rankedLeaderBoard = new ArrayList<>();
        int prevWins = -1;
        int rank = 0;
        for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
            HashMap<String, Object> userStat = new HashMap<>();
            String username = entry.getKey();
            Integer wins = entry.getValue();
            if(prevWins!=wins) {
                rank++;
                prevWins = wins;
            }
            userStat.put("wins", wins);
            userStat.put("userName", username);
            userStat.put("rank", rank);
            rankedLeaderBoard.add(userStat);
        }
        return rankedLeaderBoard;
    }

    /**
     * Sorts a leaderboard hashmap by win counts in descending order.
     *
     * This method takes a hashmap of usernames and their win counts, and sorts it by the win counts in descending order.
     * It returns a new hashmap that is sorted by the win counts.
     *
     * @param leaderboard a hashmap of usernames and their win counts
     * @return a sorted hashmap of usernames and their win counts
     */
    private HashMap<String, Integer> sortLeaderboard(HashMap<String, Integer> leaderboard) {
        return leaderboard = leaderboard.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Generates the leaderboard for users based on their win counts for a specific number of pairs.
     *
     * This method retrieves the users and their win counts, sorts the leaderboard, and creates a ranked leaderboard.
     * It returns a list of hashmaps representing the ranked leaderboard.
     *
     * @param pairs the number of pairs to filter the win counts
     * @return a list of hashmaps representing the ranked leaderboard
     */
    public List<HashMap<String, Object>> getLeaderBoard(int pairs) {
        HashMap<String, Integer> leaderboard= getUsersWithWins(pairs);
        leaderboard= sortLeaderboard(leaderboard);
        return makeRankedLeaderBoard(leaderboard);
    }
}
