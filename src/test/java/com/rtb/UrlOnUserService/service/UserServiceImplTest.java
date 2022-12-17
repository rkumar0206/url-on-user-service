package com.rtb.UrlOnUserService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.*;
import com.rtb.UrlOnUserService.exceptions.FollowerException;
import com.rtb.UrlOnUserService.exceptions.UserException;
import com.rtb.UrlOnUserService.models.*;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import com.rtb.UrlOnUserService.repository.FollowerRepository;
import com.rtb.UrlOnUserService.repository.RoleRepository;
import com.rtb.UrlOnUserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static UserAccount user;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private FollowerRepository followerRepository;
    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {

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
                null,
                new ArrayList<>()
        );

        userService = new UserServiceImpl(userRepository, roleRepository, followerRepository, confirmationTokenRepository, emailService, new ObjectMapper(), bCryptPasswordEncoder);
    }

    private void mockSecurityContextHolder() {

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        String[] roles = user.getRoles().stream().map(Role::getRoleName).toArray(String[]::new);
        stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                user.getPassword(),
                authorities);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockGetAuthenticatedUserMethod() {

        mockSecurityContextHolder();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

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
                .hasMessageContaining(accountNotVerifiedError);
    }

    @Test
    void saveUser_newUser_CorrectDetails_userIsSaved() throws Exception {

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId("test0574@example.com")
                .username("testing0206")
                .password("SOmePassword")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        userService.saveUser(userCreateRequest);

        ArgumentCaptor<UserAccount> userArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        verify(emailService, times(1)).sendConfirmationToken(any());

        UserAccount capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo(userCreateRequest.getUsername());
    }

    @Test
    void saveUser_newUser_WithEmailIdAlreadyPresent_exceptionIsThrown() {

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId(user.getEmailId())
                .username("testing0206")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.saveUser(userCreateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(duplicateEmailIdError);

    }

    @Test
    void saveUser_newUser_WithUsernameAlreadyPresent_exceptionIsThrown() {

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId("test79376@gmail.com")
                .username(user.getUsername())
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.saveUser(userCreateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(duplicateUsernameError);

    }

    @Test
    void saveUser_newUser_AlreadySavedButNotVerified_CorrectDetails_userIsSaved() throws Exception {

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
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

        userService.saveUser(userCreateRequest);

        ArgumentCaptor<UserAccount> userArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        verify(emailService, times(1)).sendConfirmationToken(any());

        UserAccount capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo(userCreateRequest.getUsername());

    }

    @Test
    void saveUser_newUser_AlreadySavedButNotVerified_UsernameDifferentAndAlreadyPresentInDb_exceptionIsThrown() {

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .firstName("TestFname")
                .lastName("TestLname")
                .emailId(user.getEmailId())
                .username("test379827972")
                .dob(new Date())
                .phoneNumber(null)
                .profileImage(null)
                .build();

        user.setAccountVerified(false);

        UserAccount user2 = new ObjectMapper().convertValue(user, UserAccount.class);

        when(userRepository.findByEmailId(anyString())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(userCreateRequest.getUsername())).thenReturn(Optional.of(user2));

        assertThatThrownBy(() -> userService.saveUser(userCreateRequest))
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

        Role role = new Role(1L, RoleNames.ADMIN);

        when(roleRepository.findByRoleName(any())).thenReturn(Optional.of(role));

        userService.addRoleToTheUser(user, "ADMIN");

        assertThat(user.getRoles()).isNotEmpty();
    }

    @Test
    void getAuthenticatedUser_userValid_Success() {

        mockSecurityContextHolder();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        UserAccount authenticatedUser = userService.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
    }

    @Test
    void getAuthenticatedUser_userNotFound_ExceptionThrown() {

        mockSecurityContextHolder();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getAuthenticatedUser())
                .isInstanceOf(UserException.class)
                .hasMessageContaining(userNotFoundError);
    }

    @Test
    void getAuthenticatedUser_userFoundButNotVerified_ExceptionThrown() {

        mockSecurityContextHolder();
        user.setAccountVerified(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getAuthenticatedUser())
                .isInstanceOf(UserException.class)
                .hasMessageContaining(accountNotVerifiedError);
    }

    @Test
    void updateUserDetails_InformationValid_UpdateSuccessful() {

        UpdateUserDetailsRequest updateUserDetailsRequest = UpdateUserDetailsRequest.builder()
                .emailId(user.getEmailId())
                .uid(user.getUid())
                .firstName("FnameUpdated")
                .dob(new Date())
                .phoneNumber("1234456789")
                .build();

        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.of(user));

        mockSecurityContextHolder();

        userService.updateUserDetails(updateUserDetailsRequest);

        ArgumentCaptor<UserAccount> argumentCaptor = ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getFirstName()).isEqualTo(updateUserDetailsRequest.getFirstName());
        assertThat(argumentCaptor.getValue().getDob()).isEqualTo(updateUserDetailsRequest.getDob());

    }

    @Test
    void updateUserDetails_UserNotPresent_exceptionIsThrown() {

        UpdateUserDetailsRequest updateUserDetailsRequest = UpdateUserDetailsRequest.builder()
                .emailId(user.getEmailId())
                .uid(user.getUid())
                .firstName("FnameUpdated")
                .dob(new Date())
                .phoneNumber("1234456789")
                .build();

        when(userRepository.findByUid(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserDetails(updateUserDetailsRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(userNotFoundError);
    }

    @Test
    void updateUserDetails_UsernameMismatch_exceptionIsThrown() {

        UserAccount user2 = new ObjectMapper().convertValue(user, UserAccount.class);
        user.setUsername("Different_username");

        UpdateUserDetailsRequest updateUserDetailsRequest = UpdateUserDetailsRequest.builder()
                .emailId(user.getEmailId())
                .uid(user.getUid())
                .firstName("FnameUpdated")
                .dob(new Date())
                .phoneNumber("1234456789")
                .build();

        mockSecurityContextHolder();
        when(userRepository.findByUid(anyString())).thenReturn(Optional.of(user2));

        assertThatThrownBy(() -> userService.updateUserDetails(updateUserDetailsRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(invalidUserAndUIDError);
    }

    @Test
    void updateUserDetails_AccountNotActivated_exceptionIsThrown() {

        user.setAccountVerified(false);

        UpdateUserDetailsRequest updateUserDetailsRequest = UpdateUserDetailsRequest.builder()
                .emailId(user.getEmailId())
                .uid(user.getUid())
                .firstName("FnameUpdated")
                .dob(new Date())
                .phoneNumber("1234456789")
                .build();

        mockSecurityContextHolder();
        when(userRepository.findByUid(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUserDetails(updateUserDetailsRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(accountNotVerifiedError);
    }

    @Test
    void changeUserEmailId_Success() throws Exception {

        ChangeUserEmailIdRequest changeUserEmailIdRequest = ChangeUserEmailIdRequest.builder()
                .requestedEmailId("newEmailId.example.com")
                .previousEmailId(user.getEmailId())
                .uid(user.getUid())
                .build();

        when(userRepository.findByEmailId(changeUserEmailIdRequest.getPreviousEmailId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailId(changeUserEmailIdRequest.getRequestedEmailId())).thenReturn(Optional.empty());
        when(userRepository.findByUid(changeUserEmailIdRequest.getUid())).thenReturn(Optional.of(user));

        mockSecurityContextHolder();

        userService.changeUserEmailId(changeUserEmailIdRequest);

        ArgumentCaptor<UserAccount> userArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        verify(emailService, times(1)).sendConfirmationToken(any());

        UserAccount capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getEmailId()).isEqualTo(changeUserEmailIdRequest.getRequestedEmailId());
    }

    @Test
    void changeUserEmailId_EmailIdAlreadyPresent_exceptionThrown() {

        ChangeUserEmailIdRequest changeUserEmailIdRequest = ChangeUserEmailIdRequest.builder()
                .requestedEmailId("newEmailId.example.com")
                .previousEmailId(user.getEmailId())
                .uid(user.getUid())
                .build();

        when(userRepository.findByEmailId(changeUserEmailIdRequest.getPreviousEmailId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailId(changeUserEmailIdRequest.getRequestedEmailId())).thenReturn(Optional.of(new UserAccount()));
        when(userRepository.findByUid(changeUserEmailIdRequest.getUid())).thenReturn(Optional.of(user));

        mockSecurityContextHolder();

        assertThatThrownBy(() -> userService.changeUserEmailId(changeUserEmailIdRequest))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(duplicateEmailIdError);

    }

    @Test
    void changeUserUsername_Success() throws Exception {

        ChangeUserUsernameRequest changeUserUsernameRequest = ChangeUserUsernameRequest.builder()
                .requestedUsername("New_username")
                .previousUsername(user.getUsername())
                .uid(user.getUid())
                .build();

        when(userRepository.findByUsername(changeUserUsernameRequest.getPreviousUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(changeUserUsernameRequest.getRequestedUsername())).thenReturn(Optional.empty());
        when(userRepository.findByUid(changeUserUsernameRequest.getUid())).thenReturn(Optional.of(user));

        mockSecurityContextHolder();

        userService.changeUserUsername(changeUserUsernameRequest);

        ArgumentCaptor<UserAccount> userArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository).save(userArgumentCaptor.capture());
        UserAccount capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo(changeUserUsernameRequest.getRequestedUsername());
    }

    @Test
    void changeUserUsername_UsernameAlreadyPresent_exceptionThrown() {

        ChangeUserUsernameRequest changeUserUsernameRequest = ChangeUserUsernameRequest.builder()
                .requestedUsername("New_username")
                .previousUsername(user.getUsername())
                .uid(user.getUid())
                .build();

        when(userRepository.findByUsername(changeUserUsernameRequest.getPreviousUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(changeUserUsernameRequest.getRequestedUsername())).thenReturn(Optional.of(new UserAccount()));
        when(userRepository.findByUid(changeUserUsernameRequest.getUid())).thenReturn(Optional.of(user));

        mockSecurityContextHolder();

        assertThatThrownBy(() -> userService.changeUserUsername(changeUserUsernameRequest))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(duplicateUsernameError);

    }

    @Test
    void followUser_EverythingValid_Success() {

        mockGetAuthenticatedUserMethod();

        UserAccount followingUser = new UserAccount(
                "followingEmail",
                "followingUsername",
                "followingPassword",
                "followingUid000",
                "firstName",
                "lastName",
                "",
                "",
                new Date(),
                true,
                null
        );

        when(userRepository.findByUid(followingUser.getUid())).thenReturn(Optional.of(followingUser));
        when(followerRepository.findByFollowerUidAndFollowingUid(anyString(), anyString()))
                .thenReturn(Optional.empty());

        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid(followingUser.getUid()).build();

        userService.followUser(followAndUnfollowRequest);

        ArgumentCaptor<Follower> followerArgumentCaptor = ArgumentCaptor.forClass(Follower.class);
        verify(followerRepository).save(followerArgumentCaptor.capture());

        Follower capturedValue = followerArgumentCaptor.getValue();

        assertThat(capturedValue.getFollowerUid()).isEqualTo(user.getUid());
        assertThat(capturedValue.getFollowingUid()).isEqualTo(followingUser.getUid());
    }

    @Test
    void followUser_FollowingUserNotFound_ExceptionThrown() {

        mockGetAuthenticatedUserMethod();

        when(userRepository.findByUid(anyString())).thenReturn(Optional.empty());

        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid("something").build();


        assertThatThrownBy(() -> userService.followUser(followAndUnfollowRequest))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(userWithUidNotFoundError, "something"));
    }


    @Test
    void followUser_FollowerAccountNotVerified_ExceptionThrown() {

        mockGetAuthenticatedUserMethod();

        UserAccount followingUser = new UserAccount(
                "followingEmail",
                "followingUsername",
                "followingPassword",
                "followingUid000",
                "firstName",
                "lastName",
                "",
                "",
                new Date(),
                false,
                null
        );

        when(userRepository.findByUid(followingUser.getUid())).thenReturn(Optional.of(followingUser));

        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid(followingUser.getUid()).build();

        assertThatThrownBy(() -> userService.followUser(followAndUnfollowRequest))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(accountNotVerifiedForUidError, followingUser.getUid()));
    }

    @Test
    void followUser_RecordAlreadyPresent_ExceptionThrown() {

        mockGetAuthenticatedUserMethod();

        UserAccount followingUser = new UserAccount(
                "followingEmail",
                "followingUsername",
                "followingPassword",
                "followingUid000",
                "firstName",
                "lastName",
                "",
                "",
                new Date(),
                true,
                null
        );

        when(userRepository.findByUid(followingUser.getUid())).thenReturn(Optional.of(followingUser));
        when(followerRepository.findByFollowerUidAndFollowingUid(anyString(), anyString()))
                .thenReturn(Optional.of(new Follower()));

        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid(followingUser.getUid()).build();

        assertThatThrownBy(() -> userService.followUser(followAndUnfollowRequest))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(userAlreadyFollowingErrorMessage, followingUser.getUsername(), user.getUsername()));
    }

    @Test
    void unfollowUser_EverythingValid_Success() {

        mockGetAuthenticatedUserMethod();
        when(userRepository.findByUid(anyString())).thenReturn(Optional.of(new UserAccount()));
        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid("something").build();

        userService.unfollowUser(followAndUnfollowRequest);
        verify(followerRepository, times(1))
                .deleteByFollowingUidAndFollowerUid(anyString(), anyString());
    }

    @Test
    void unfollowUser_FollowingUserNotFound_ExceptionThrown() {

        mockGetAuthenticatedUserMethod();
        when(userRepository.findByUid(anyString())).thenReturn(Optional.empty());

        FollowAndUnfollowRequest followAndUnfollowRequest = FollowAndUnfollowRequest.builder()
                .followingUid("something").build();

        assertThatThrownBy(() -> userService.unfollowUser(followAndUnfollowRequest))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(userWithUidNotFoundError, followAndUnfollowRequest.getFollowingUid()));

        verify(followerRepository, times(0))
                .deleteByFollowingUidAndFollowerUid(anyString(), anyString());
    }

    @Test
    void getAllFollowersOfUser_Success() {

        List<UserAccount> userAccounts = Arrays.asList(new UserAccount(), new UserAccount());

        when(userRepository.findByUid(anyString())).thenReturn(Optional.of(new UserAccount()));

        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findAllFollowersOfUser(user.getUid(), pageable))
                .thenReturn(new PageImpl<>(userAccounts));

        userService.getAllFollowersOfUser(user.getUid(), pageable);

        verify(userRepository, times(1))
                .findAllFollowersOfUser(anyString(), any());
    }

    @Test
    void getAllFollowersOfUser_UserNotFound_ExceptionThrown() {

        PageRequest pageable = PageRequest.of(0, 10);

        when(userRepository.findByUid(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getAllFollowersOfUser(user.getUid(), pageable))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(userWithUidNotFoundError, user.getUid()));

        verify(userRepository, times(0))
                .findAllFollowersOfUser(anyString(), any());
    }

    @Test
    void getAllFollowersOfUser_UIDNotValid_ExceptionThrown() {

        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> userService.getAllFollowersOfUser("", pageable))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining("User uid is invalid");

        verify(userRepository, times(0))
                .findAllFollowersOfUser(anyString(), any());
    }

    @Test
    void getAllFollowingsOfUser_Success() {

        PageRequest pageable = PageRequest.of(0, 10);
        List<UserAccount> userAccounts = Arrays.asList(new UserAccount(), new UserAccount());

        when(userRepository.findByUid(anyString())).thenReturn(Optional.of(new UserAccount()));

        when(userRepository.findAllUserAccountsUserIsFollowing(user.getUid(), pageable))
                .thenReturn(new PageImpl<>(userAccounts));

        userService.getAllFollowingsOfUser(user.getUid(), pageable);

        verify(userRepository, times(1))
                .findAllUserAccountsUserIsFollowing(anyString(), any());
    }

    @Test
    void getAllFollowingsOfUser_UserNotFound_ExceptionThrown() {

        PageRequest pageable = PageRequest.of(0, 10);

        when(userRepository.findByUid(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getAllFollowingsOfUser(user.getUid(), pageable))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining(String.format(userWithUidNotFoundError, user.getUid()));

        verify(userRepository, times(0))
                .findAllUserAccountsUserIsFollowing(anyString(), any());
    }

    @Test
    void getAllFollowingsOfUser_UIDNotValid_ExceptionThrown() {

        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> userService.getAllFollowingsOfUser("", pageable))
                .isInstanceOf(FollowerException.class)
                .hasMessageContaining("User uid is invalid");

        verify(userRepository, times(0))
                .findAllUserAccountsUserIsFollowing(anyString(), any());
    }

}