package com.memo.game.service;

import com.memo.game.entity.MemoUser;
import com.memo.game.repo.MemoUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    private UserService userService;
    @Mock
    private MemoUserRepository memoUserRepository;
    private final String name1 = "testName001";
    private final String email1 = "testname@gmail.com";
    private final String name2 = "testName002";
    private final String email2 = "testname2@gmail.com";
    private final UUID id1 = UUID.randomUUID();
    private final UUID id2 = UUID.randomUUID();
    private final MemoUser user1 = new MemoUser(name1, email1, "password");
    private final MemoUser user2 = new MemoUser(name2, email2, "password");
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        user1.setId(id1);
        user2.setId(id2);

        when(memoUserRepository.findById(id1)).thenReturn(Optional.of(user1));
        when(memoUserRepository.findById(id2)).thenReturn(Optional.of(user2));

        when(memoUserRepository.findByUserName(name1)).thenReturn(user1);
        when(memoUserRepository.findByUserName(name2)).thenReturn(user2);

        when(memoUserRepository.findByEmail(email1)).thenReturn(user1);
        when(memoUserRepository.findByEmail(email2)).thenReturn(user2);

        when(memoUserRepository.save(user1)).thenReturn(user1);
        when(memoUserRepository.save(user2)).thenReturn(user2);

        userService = new UserService(memoUserRepository);
    }

    @Test
    public void getByUserNameTest() {
        MemoUser userByName = userService.getByUserName(name2);
        assertThat(userByName).isNotNull();
        assertThat(userByName.getId()).isEqualTo(user2.getId());
        assertThat(userByName.getId()).isEqualTo(user2.getId());
        assertThat(userByName.getEmail()).isEqualTo(user2.getEmail());
        assertThat(userByName.getUserName()).isEqualTo(user2.getUserName());
    }

    @Test
    public void getByEmailTest() {
        MemoUser userByEmail = userService.getByEmail(email2);
        assertThat(userByEmail).isNotNull();
        assertThat(userByEmail.getId()).isEqualTo(user2.getId());
        assertThat(userByEmail.getEmail()).isEqualTo(user2.getEmail());
        assertThat(userByEmail.getUserName()).isEqualTo(user2.getUserName());
    }

    @Test
    public void getUserNameByIdTest() {
        String userName = userService.getUserNameById(user1.getId());
        assertThat(userName).isEqualTo(name1);
    }
}
