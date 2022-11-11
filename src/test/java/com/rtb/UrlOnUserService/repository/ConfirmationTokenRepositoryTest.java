package com.rtb.UrlOnUserService.repository;

import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConfirmationTokenRepositoryTest {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    private ConfirmationToken confirmationToken;

    @BeforeEach
    void setUp() {

        confirmationTokenRepository.deleteAll();

        confirmationToken = new ConfirmationToken("example@test.com");

        confirmationTokenRepository.save(confirmationToken);
    }

    @Test
    void findByConfirmationToken() {

        Optional<ConfirmationToken> expectedConfirmationToken = confirmationTokenRepository.findByConfirmationToken(confirmationToken.getConfirmationToken());

        assertThat(expectedConfirmationToken).isPresent();
    }

    @Test
    void findByUserEmailId() {

        Optional<ConfirmationToken> expectedConfirmationToken = confirmationTokenRepository.findByUserEmailId(confirmationToken.getUserEmailId());
        assertThat(expectedConfirmationToken).isPresent();
    }
}