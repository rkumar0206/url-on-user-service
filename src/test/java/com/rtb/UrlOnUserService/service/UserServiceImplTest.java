package com.rtb.UrlOnUserService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.models.UserRequest;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import com.rtb.UrlOnUserService.repository.RoleRepository;
import com.rtb.UrlOnUserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static UrlOnUser user;
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        user = new UrlOnUser(
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
                null,
                new ArrayList<>()
        );

        objectMapper = new ObjectMapper();
        userService = new UserServiceImpl(userRepository, roleRepository, confirmationTokenRepository, emailService, objectMapper, bCryptPasswordEncoder);
    }

    @Test
    void loadUserByUsername_searchByUsername_user_not_null() {

        when(userRepository.findByUsername("test0206")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test0206");

        assertNotNull(userDetails);
    }

    @Test
    void loadUserByUsername_searchByEmailId_user_not_null() {

        when(userRepository.findByEmailId("test123@gmail.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test123@gmail.com");

        assertNotNull(userDetails);
    }

    @Test
    void loadUserByUsername_user_null_exceptionThrown() {

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("test123@gmail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(userNotFoundError);

    }

    @Test
    void loadUserByUsername_user_account_not_verified_exceptionThrown() {

        user.setAccountVerified(false);
        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.loadUserByUsername("test123@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(accountNotVerified);
    }

    @Test
    void saveUser_newUser_CorrectDetails_userIsSaved() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId("test0574@example.com")
                .username("testing0206")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        userService.saveUser(userRequest);

        ArgumentCaptor<UrlOnUser> userArgumentCaptor = ArgumentCaptor.forClass(UrlOnUser.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        verify(emailService, times(1)).sendConfirmationToken(any());

        UrlOnUser capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo(userRequest.getUsername());


    }

    @Test
    void saveUser_newUser_WithEmailIdAlreadyPresent_exceptionIsThrown() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId(user.getEmailId())
                .username("testing0206")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.saveUser(userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(duplicateEmailIdError);

    }

    @Test
    void saveUser_newUser_WithUsernameAlreadyPresent_exceptionIsThrown() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId("test79376@gmail.com")
                .username(user.getUsername())
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.saveUser(userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(duplicateUsernameError);

    }

    @Test
    void saveUser_newUser_AlreadySavedButNotVerified_CorrectDetails_userIsSaved() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId(user.getEmailId())
                .username("test379827972")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        user.setAccountVerified(false);

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        userService.saveUser(userRequest);

        ArgumentCaptor<UrlOnUser> userArgumentCaptor = ArgumentCaptor.forClass(UrlOnUser.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        verify(emailService, times(1)).sendConfirmationToken(any());

        UrlOnUser capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo(userRequest.getUsername());

    }

    @Test
    void saveUser_newUser_AlreadySavedButNotVerified_UsernameDifferentAndAlreadyPresentInDb_exceptionIsThrown() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId(user.getEmailId())
                .username("test379827972")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        user.setAccountVerified(false);

        UrlOnUser user2 = objectMapper.convertValue(user, UrlOnUser.class);

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.of(user2));

        assertThatThrownBy(() -> userService.saveUser(userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(duplicateUsernameError);
    }

    @Test
    void verifyAccount_confirmationTokenAndUserPresent_verifiedMessage() {

        user.setAccountVerified(false);
        ConfirmationToken confirmationToken = new ConfirmationToken(user.getEmailId());

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findByConfirmationToken(anyString())).thenReturn(Optional.of(confirmationToken));

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(anyString());

        assertThat(AccountVerificationMessage.VERIFIED).isEqualTo(accountVerificationMessage);
        assertThat(true).isEqualTo(user.isAccountVerified());

    }

    @Test
    void verifyAccount_confirmationTokenAndUserPresent_UserAlreadyVerified_alreadyVerifiedMessage() {

        user.setAccountVerified(true);
        ConfirmationToken confirmationToken = new ConfirmationToken(user.getEmailId());

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findByConfirmationToken(anyString())).thenReturn(Optional.of(confirmationToken));

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(anyString());

        assertThat(AccountVerificationMessage.ALREADY_VERIFIED).isEqualTo(accountVerificationMessage);
        assertThat(true).isEqualTo(user.isAccountVerified());

    }

    @Test
    void verifyAccount_confirmationTokenPresentButUserNotPresent_invalidMessage() {

        ConfirmationToken confirmationToken = new ConfirmationToken(user.getEmailId());

        when(confirmationTokenRepository.findByConfirmationToken(anyString())).thenReturn(Optional.of(confirmationToken));
        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.empty());

        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(anyString());
        assertThat(AccountVerificationMessage.INVALID).isEqualTo(accountVerificationMessage);
    }

    @Test
    void verifyAccount_confirmationTokenNotPresent_invalidMessage() {

        when(confirmationTokenRepository.findByConfirmationToken(anyString())).thenReturn(Optional.empty());
        AccountVerificationMessage accountVerificationMessage = userService.verifyAccount(anyString());
        assertThat(AccountVerificationMessage.INVALID).isEqualTo(accountVerificationMessage);
    }

    @Test
    void updatePassword_userPresent_updateSuccessful() {

        String oldPassword = user.getPassword();
        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.of(user));

        userService.updateUserPassword(user.getUid(), anyString());

        assertThat(user.getPassword()).isNotEqualToIgnoringWhitespace(oldPassword);

    }

    @Test
    void updatePassword_userNotPresent_exceptionThrown() {

        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserPassword(user.getUid(), anyString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(userNotFoundError);
    }

    @Test
    void updateUserResetPasswordToken_userPresent_updateSuccessful() {

        user.setResetPasswordToken("");
        String oldResetPasswordToken = user.getResetPasswordToken();
        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.of(user));
        userService.updateUserResetPasswordToken(user.getUid(), UUID.randomUUID().toString());

        assertThat(user.getResetPasswordToken()).isNotEqualToIgnoringWhitespace(oldResetPasswordToken);
    }

    @Test
    void updateUserResetPasswordToken_userNotPresent_exceptionThrown() {

        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUserResetPasswordToken(user.getUid(), anyString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(userNotFoundError);
    }

    @Test
    void addRoleToTheUser_success() {

        Role role = new Role(1L, "ROLE_ADMIN");

        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));

        userService.addRoleToTheUser(user, "ROLE_ADMIN");

        assertThat(user.getRoles()).isNotEmpty();
    }
}