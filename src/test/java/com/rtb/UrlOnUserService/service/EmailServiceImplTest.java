package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private HttpServletRequest request;

    private UrlOnUser user;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {

        user = new UrlOnUser(
                null,
                "test123@examplertb.test",
                "test0206",
                "test@password",
                "ttttt",
                "TestFirstName",
                "TestLastName",
                null,
                null,
                new Date(),
                true,
                null,
                new ArrayList<>()
        );

        emailService = new EmailServiceImpl(confirmationTokenRepository, javaMailSender, request);

    }

    @Test
    void sendConfirmationToken_whenTokenIsNotPresentInDb() throws Exception {

        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:1234"));
        when(request.getServletPath()).thenReturn("");

        when(confirmationTokenRepository.findByUserEmailId(user.getEmailId())).thenReturn(Optional.empty());

        emailService.sendConfirmationToken(user);

        ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor = ArgumentCaptor.forClass(ConfirmationToken.class);
        verify(confirmationTokenRepository, times(1)).save(confirmationTokenArgumentCaptor.capture());
    }

    @Test
    void sendConfirmationToken_whenTokenIsPresentInDb() throws Exception {

        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:1234"));
        when(request.getServletPath()).thenReturn("");

        when(confirmationTokenRepository.findByUserEmailId(user.getEmailId())).thenReturn(Optional.of(new ConfirmationToken(anyString())));

        emailService.sendConfirmationToken(user);

        verify(confirmationTokenRepository, times(1)).findByUserEmailId(anyString());

        ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor = ArgumentCaptor.forClass(ConfirmationToken.class);
        verify(confirmationTokenRepository, times(0)).save(confirmationTokenArgumentCaptor.capture());
    }

    @Test
    void sendPasswordResetUrl() throws Exception {

        SimpleMailMessage simpleMailMessage = emailService.sendPasswordResetUrl(user, anyString());
        verify(javaMailSender, times(1)).send(simpleMailMessage);
    }

    @Test
    void sendMail() {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        doThrow(RuntimeException.class).when(javaMailSender).send(simpleMailMessage);

        assertThatThrownBy(() -> emailService.sendMail(simpleMailMessage))
                .isInstanceOf(RuntimeException.class);
    }
}