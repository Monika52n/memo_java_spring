package com.memo.game.service;

import com.memo.game.entity.MemoUsers;
import com.memo.game.repo.MemoMultiGameRepository;
import com.memo.game.repo.MemoUsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MultiPlayerStatServiceTest {
    @Mock
    private static MemoMultiGameRepository memoMultiGameRepository;
    @Mock
    private static MemoUsersRepository memoUsersRepository;
    private static MultiPlayerStatService multiPlayerStatService;
    private static final List<MemoUsers> userList = new ArrayList<>();
    private static final Integer numOfUsers = 6;
    @BeforeAll
    static public void setUp() {
        memoMultiGameRepository = mock(MemoMultiGameRepository.class);
        memoUsersRepository = mock(MemoUsersRepository.class);

        for(int i=0; i<numOfUsers; i++) {
            MemoUsers user = new MemoUsers("testName" + i, "testName" + i + "@gmail.com", "password");
            user.setId(UUID.randomUUID());
            userList.add(user);
        }
        when(memoUsersRepository.findAll()).thenReturn(userList);

        for(int i=0; i<numOfUsers; i++) {
            when(memoMultiGameRepository.getWins(
                    userList.get(i).getId().toString(),
                    8
            )).thenReturn(i*5+1);

            when(memoMultiGameRepository.getWins(
                    userList.get(i).getId().toString(),
                    16
            )).thenReturn(5);

            when(memoMultiGameRepository.getWins(
                    userList.get(i).getId().toString(),
                    24
            )).thenReturn(i-i%2);
        }

        multiPlayerStatService = new MultiPlayerStatService(memoMultiGameRepository, memoUsersRepository);
    }

    @Test
    public void getLeaderBoardDifferentRanksTest() {
        List<HashMap<String, Object>> leaderBoard = multiPlayerStatService.getLeaderBoard(8);
        int i = numOfUsers-1;
        for(HashMap<String, Object> item : leaderBoard) {
            assertThat(item.get("userName")).isEqualTo("testName" + i);
            assertThat(item.get("wins")).isEqualTo(i * 5 + 1);
            assertThat(item.get("rank")).isEqualTo(numOfUsers - i);
            i--;
        }
    }

    @Test
    public void getLeaderBoardAllSameRankTest() {
        List<HashMap<String, Object>> leaderBoard = multiPlayerStatService.getLeaderBoard(16);
        for(HashMap<String, Object> item : leaderBoard) {
            assertThat(item.get("wins")).isEqualTo(5);
            assertThat(item.get("rank")).isEqualTo(1);
        }
    }

    @Test
    public void getLeaderBoardDifferentAndSameRanksToo() {
        List<HashMap<String, Object>> leaderBoard = multiPlayerStatService.getLeaderBoard(24);
        int i = numOfUsers-1;
        boolean isFirstIt = true;
        int rank = 1;
        for(HashMap<String, Object> item : leaderBoard) {
            assertThat(item.get("wins")).isEqualTo(i-i%2);
            assertThat(item.get("rank")).isEqualTo(rank);
            if(isFirstIt) {
                isFirstIt = false;
            } else {
                isFirstIt = true;
                rank++;
            }
            i--;
        }
    }
}
