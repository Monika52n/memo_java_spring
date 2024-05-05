package com.memo.game;

import com.memo.game.entity.MemoUsers;
import com.memo.game.model.MultiPlayer;
import com.memo.game.model.SinglePlayer;
import com.memo.game.model.SinglePlayerTest;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.MemoUsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class GameApplicationTests {
	@Test
	void contextLoads() {
	}
}
