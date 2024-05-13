package com.memo.game.service;

import com.memo.game.entity.MemoMultiGame;
import com.memo.game.entity.MemoUsers;
import com.memo.game.repo.MemoMultiGameRepository;
import com.memo.game.repo.MemoUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultiPlayerStatService {
    private final MemoMultiGameRepository memoMultiGameRepository;
    private final MemoUsersRepository memoUsersRepository;
    @Autowired
    public  MultiPlayerStatService(MemoMultiGameRepository memoMultiGameRepository, MemoUsersRepository memoUsersRepository) {
        this.memoMultiGameRepository = memoMultiGameRepository;
        this.memoUsersRepository = memoUsersRepository;
    }

    private HashMap<String, Integer> getUsersWithWins(int pairs) {
        HashMap<String, Integer> leaderBoard = new HashMap<>();
        List<MemoUsers> users = memoUsersRepository.findAll();
        for(MemoUsers user : users) {
            Integer wins = memoMultiGameRepository.getWins((user.getId()).toString(), pairs);
            leaderBoard.put(user.getUserName(), wins);
        }
        return leaderBoard;
    }

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

    public List<HashMap<String, Object>> getLeaderBoard(int pairs) {
        HashMap<String, Integer> leaderboard= getUsersWithWins(pairs);
        leaderboard= sortLeaderboard(leaderboard);
        return makeRankedLeaderBoard(leaderboard);
    }
}
