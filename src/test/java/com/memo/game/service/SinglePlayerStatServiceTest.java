package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.dto.ModeStat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class SinglePlayerStatServiceTest {
    private SinglePlayerStatService singlePlayerStatService;
    private static List<MemoSingleGame> gameList = new ArrayList<>();

    private static int switchPairs(int pairs) {
        return switch (pairs) {
            case 8 -> 16;
            case 16 -> 24;
            case 24 -> 8;
            default -> pairs;
        };
    }

    @BeforeAll
    static public void setUp() {
        UUID userId = UUID.randomUUID();
        boolean won = false;
        int pairs = 24;
        for(int i=0; i<144; i++) {
            won = i%5!=0;
            pairs = switchPairs(pairs);
            MemoSingleGame memoSingleGame = new MemoSingleGame(
                    UUID.randomUUID(),
                    userId,
                    won,
                    i*5,
                    pairs,
                    pairs*10*(i%2+1)
            );
            gameList.add(memoSingleGame);
        }
    }

    @Test
    public void isGameModesHaveACorrectLength() {
        singlePlayerStatService = new SinglePlayerStatService();
        List<ModeStat> modeStats = singlePlayerStatService.addList(gameList);

        assertThat(modeStats.size()).isEqualTo(6);
        ModeStat modeStat1 = modeStats.get(0);
    }

    @Test
    public void isMode1StatsCorrect() {
        singlePlayerStatService = new SinglePlayerStatService();
        List<ModeStat> modeStats = singlePlayerStatService.addList(gameList);
        ModeStat modeStat1 = modeStats.get(0);

        assertThat(modeStat1.getNumOfGames()).isEqualTo(24);
        assertThat(modeStat1.getLosses()).isEqualTo(5);
        assertThat(modeStat1.getWins()).isEqualTo(19);
        assertThat((int)modeStat1.getWinningRate()).isEqualTo(79);
        assertThat(modeStat1.getPairs()).isEqualTo(8);
        assertThat(modeStat1.getTime()).isEqualTo(80);
        assertThat((int)modeStat1.getAvgRemainingTime()).isEqualTo(356);
    }

    @Test
    public void isMode2StatsCorrect() {
        singlePlayerStatService = new SinglePlayerStatService();
        List<ModeStat> modeStats = singlePlayerStatService.addList(gameList);
        ModeStat modeStat2 = modeStats.get(1);

        assertThat(modeStat2.getNumOfGames()).isEqualTo(24);
        assertThat(modeStat2.getLosses()).isEqualTo(4);
        assertThat(modeStat2.getWins()).isEqualTo(20);
        assertThat((int)modeStat2.getWinningRate()).isEqualTo(83);
        assertThat(modeStat2.getPairs()).isEqualTo(16);
        assertThat(modeStat2.getTime()).isEqualTo(320);
        assertThat(modeStat2.getAvgRemainingTime()).isEqualTo(350.0);
    }

    @Test
    public void isMode3StatsCorrect() {
        singlePlayerStatService = new SinglePlayerStatService();
        List<ModeStat> modeStats = singlePlayerStatService.addList(gameList);
        ModeStat modeStat3 = modeStats.get(2);

        assertThat(modeStat3.getNumOfGames()).isEqualTo(24);
        assertThat(modeStat3.getLosses()).isEqualTo(5);
        assertThat(modeStat3.getWins()).isEqualTo(19);
        assertThat((int)modeStat3.getWinningRate()).isEqualTo(79);
        assertThat(modeStat3.getPairs()).isEqualTo(24);
        assertThat(modeStat3.getTime()).isEqualTo(240);
        assertThat((int)modeStat3.getAvgRemainingTime()).isEqualTo(343);
    }
}
