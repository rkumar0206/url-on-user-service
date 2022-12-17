package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.Follower;
import com.rtb.UrlOnUserService.domain.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowerRepository followerRepository;

    private UserAccount user;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        user = new UserAccount(
                null,
                "test123@example.com",
                "test0206",
                "test@password",
                "ttttt",
                "TestFirstName",
                "TestLastName",
                null,
                null,
                new Date(),
                true,
                UUID.randomUUID().toString(),
                new ArrayList<>()
        );

        userRepository.save(user);

        List<String> userIds = Arrays.asList("t0001", "t0002", "t0003", "t0004", "t0005");
        for (int i = 0; i < userIds.size(); i++) {

            UserAccount userTemp = new UserAccount(
                    "user" + (i + 1) + "@gmail.com",
                    "t020" + (i + 1),
                    "examplePassword",
                    userIds.get(i),
                    "User" + (i + 1),
                    "lastName",
                    null,
                    null,
                    new Date(),
                    true,
                    null
            );

            Follower follower =
                    new Follower(i % 2 == 0 ? user.getUid() : userIds.get(i),
                            i % 2 == 0 ? userIds.get(i) : user.getUid());

            followerRepository.save(follower);
            userRepository.save(userTemp);
        }
    }

    @Test
    void findByUsername() {

        Optional<UserAccount> expectedUser = userRepository.findByUsername(user.getUsername());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByEmailId() {

        Optional<UserAccount> expectedUser = userRepository.findByEmailId(user.getEmailId());

        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByUid() {

        Optional<UserAccount> expectedUser = userRepository.findByUid(user.getUid());
        assertThat(expectedUser).isPresent();
    }

    @Test
    void findByResetPasswordToken() {
        Optional<UserAccount> expectedUser = userRepository.findByResetPasswordToken(user.getResetPasswordToken());
        assertThat(expectedUser).isPresent();
    }

    @Test
    void findAllFollowersOfUser() {

        Page<UserAccount> actualResult = userRepository.findAllFollowersOfUser(user.getUid(), PageRequest.of(0, 10));
        System.out.println(actualResult.getContent());
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).allMatch(u -> !u.getUid().equals(user.getUid()));
    }

    @Test
    void findAllUserAccountsUserIsFollowing() {

        Page<UserAccount> actualResult = userRepository.findAllUserAccountsUserIsFollowing(user.getUid(), PageRequest.of(0, 10));
        System.out.println(actualResult.getContent());
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).allMatch(u -> !u.getUid().equals(user.getUid()));
    }
}