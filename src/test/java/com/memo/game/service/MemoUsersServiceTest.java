package com.memo.game.service;

import com.memo.game.entity.MemoUsers;
import com.memo.game.repo.MemoUsersRepository;
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
public class MemoUsersServiceTest {
    MemoUsersService memoUsersService;
    @Mock
    private MemoUsersRepository memoUsersRepository;
    final String name1 = "testName001";
    final String email1 = "testname@gmail.com";
    final String name2 = "testName002";
    final String email2 = "testname2@gmail.com";
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    MemoUsers user1 = new MemoUsers(name1, email1, "password");
    MemoUsers user2 = new MemoUsers(name2, email2, "password");
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        memoUsersService = new MemoUsersService(memoUsersRepository);
        user1.setId(id1);
        user2.setId(id2);

        when(memoUsersRepository.findById(id1)).thenReturn(Optional.of(user1));
        when(memoUsersRepository.findById(id2)).thenReturn(Optional.of(user2));

        when(memoUsersRepository.findByUserName(name1)).thenReturn(user1);
        when(memoUsersRepository.findByUserName(name2)).thenReturn(user2);

        when(memoUsersRepository.findByEmail(email1)).thenReturn(user1);
        when(memoUsersRepository.findByEmail(email2)).thenReturn(user2);

        when(memoUsersRepository.save(user1)).thenReturn(user1);
        when(memoUsersRepository.save(user2)).thenReturn(user2);
    }

    @Test
    void getByUserNameTest() {
        MemoUsers userByName = memoUsersService.getByUserName(name2);
        assertThat(userByName).isNotNull();
        assertThat(userByName.getId()).isEqualTo(user2.getId());
        assertThat(userByName.getId()).isEqualTo(user2.getId());
        assertThat(userByName.getEmail()).isEqualTo(user2.getEmail());
        assertThat(userByName.getUserName()).isEqualTo(user2.getUserName());
    }

    @Test
    void getByEmailTest() {
        MemoUsers userByEmail = memoUsersService.getByEmail(email2);
        assertThat(userByEmail).isNotNull();
        assertThat(userByEmail.getId()).isEqualTo(user2.getId());
        assertThat(userByEmail.getEmail()).isEqualTo(user2.getEmail());
        assertThat(userByEmail.getUserName()).isEqualTo(user2.getUserName());
    }

    @Test
    void getUserNameByIdTest() {
        String userName = memoUsersService.getUserNameById(user1.getId());
        assertThat(userName).isEqualTo(name1);
    }

    @Test
    void signInSignOutTest() {
        assertThat(memoUsersService.signIn(user1.getId())).isEqualTo(true);
        assertThat(memoUsersService.isSignedIn(user1.getId())).isEqualTo(true);
        assertThat(memoUsersService.signOut(user1.getId())).isEqualTo(true);
        assertThat(memoUsersService.isSignedIn(user1.getId())).isEqualTo(false);
    }
}
