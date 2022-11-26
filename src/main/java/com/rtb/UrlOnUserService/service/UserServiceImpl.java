package com.rtb.UrlOnUserService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.models.ChangeUserEmailIdRequest;
import com.rtb.UrlOnUserService.models.ChangeUserUsernameRequest;
import com.rtb.UrlOnUserService.models.UpdateUserDetailsRequest;
import com.rtb.UrlOnUserService.models.UserCreateRequest;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import com.rtb.UrlOnUserService.repository.RoleRepository;
import com.rtb.UrlOnUserService.repository.UserRepository;
import com.rtb.UrlOnUserService.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserAccount user = getUserByEmailIdOrByUsername(username);

        if (user == null) {

            throw new UsernameNotFoundException(userNotFoundError);
        } else if (!user.isAccountVerified()) {
            throw new RuntimeException(accountNotVerifiedError);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getRoleName())));

        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public UserAccount getUserByEmailIdOrByUsername(String username) {

        if (Utility.isValidEmailAddress(username)) {

            return getUserByEmailId(username);
        } else {

            return getUserByUserName(username);
        }
    }

    @Override
    public UserAccount getUserByResetPasswordToken(String resetPasswordUrl) {

        return userRepository.findByResetPasswordToken(resetPasswordUrl).orElse(null);
    }

    @Override
    public UserAccount saveUser(UserCreateRequest userCreateRequest) {

        UserAccount user;
        Optional<UserAccount> tempUser = userRepository.findByEmailId(userCreateRequest.getEmailId());

        if (tempUser.isPresent() && !tempUser.get().isAccountVerified()) {

            user = tempUser.get();

            user.setPassword(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()));

            if (!user.getUsername().trim().equals(userCreateRequest.getUsername().trim())) {

                if (userRepository.findByUsername(userCreateRequest.getUsername()).isPresent()) {
                    throw new RuntimeException(duplicateUsernameError);
                }
            }

            user.setDob(userCreateRequest.getDob());
            user.setFirstName(userCreateRequest.getFirstName());
            user.setLastName(userCreateRequest.getLastName());
            user.setPhoneNumber(userCreateRequest.getPhoneNumber());
            user.setProfileImage(userCreateRequest.getProfileImage());
            user.setUsername(userCreateRequest.getUsername());

            userRepository.save(user);

            try {
                emailService.sendConfirmationToken(user);
            } catch (Exception exception) {
                log.error(sendingMailError, exception);
                throw new RuntimeException(sendingMailError);
            }
            log.info("Confirmation token sent");
        } else {

            user = objectMapper.convertValue(userCreateRequest, UserAccount.class);

            if (userRepository.findByEmailId(user.getEmailId().trim()).isPresent()) {

                throw new RuntimeException(duplicateEmailIdError);
            }

            if (userRepository.findByUsername(user.getUsername().trim()).isPresent()) {

                throw new RuntimeException(duplicateUsernameError);
            }

            user.setUid(UUID.randomUUID().toString().replace("-", ""));
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setAccountVerified(false);

            userRepository.save(user);

            try {
                emailService.sendConfirmationToken(user);
            } catch (Exception exception) {
                log.error(sendingMailError, exception);
                throw new RuntimeException(sendingMailError);
            }
        }

        return user;
    }

    @Override
    public UserAccount updateUserDetails(UpdateUserDetailsRequest updateUserDetailsRequest) {

        Optional<UserAccount> userByUid = userRepository.findByUid(updateUserDetailsRequest.getUid());
        Optional<UserAccount> userByEmail = userRepository.findByEmailId(updateUserDetailsRequest.getEmailId());

        if (!userByEmail.isPresent() || !userByUid.isPresent()) {

            throw new RuntimeException(userNotFoundError);
        } else {

            validateUserForUpdate(userByEmail.get(), userByUid.get());

            userByUid.get().setFirstName(updateUserDetailsRequest.getFirstName());
            userByUid.get().setLastName(updateUserDetailsRequest.getLastName());
            userByUid.get().setProfileImage(updateUserDetailsRequest.getProfileImage());
            userByUid.get().setPhoneNumber(updateUserDetailsRequest.getPhoneNumber());
            userByUid.get().setDob(updateUserDetailsRequest.getDob());

            try {
                userRepository.save(userByUid.get());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return userByUid.orElseThrow(() -> new RuntimeException(userNotFoundError));
    }

    @Override
    public UserAccount changeUserEmailId(ChangeUserEmailIdRequest changeUserEmailIdRequest) {

        Optional<UserAccount> userByUid = userRepository.findByUid(changeUserEmailIdRequest.getUid());
        Optional<UserAccount> userByEmail = userRepository.findByEmailId(changeUserEmailIdRequest.getPreviousEmailId());

        if (!userByEmail.isPresent() || !userByUid.isPresent()) {
            throw new RuntimeException(userNotFoundError);
        } else {

            validateUserForUpdate(userByEmail.get(), userByUid.get());

            log.info("User is valid for updating the details.");

            if (userRepository.findByEmailId(changeUserEmailIdRequest.getRequestedEmailId().trim()).isPresent()) {

                throw new RuntimeException(duplicateEmailIdError);
            } else {

                log.info("Changing user email id");

                userByEmail.get().setEmailId(changeUserEmailIdRequest.getRequestedEmailId().trim());
                userByEmail.get().setAccountVerified(false);

                userRepository.save(userByEmail.get());
                try {
                    emailService.sendConfirmationToken(userByEmail.get());
                } catch (Exception exception) {
                    log.error(sendingMailError, exception);
                    throw new RuntimeException(sendingMailError);
                }
            }
        }
        return userByEmail.orElseThrow(() -> new RuntimeException(userNotFoundError));
    }

    @Override
    public UserAccount changeUserUsername(ChangeUserUsernameRequest changeUserUsernameRequest) {

        Optional<UserAccount> userByUid = userRepository.findByUid(changeUserUsernameRequest.getUid());
        Optional<UserAccount> userByUsername = userRepository.findByUsername(changeUserUsernameRequest.getPreviousUsername());

        if (!userByUsername.isPresent() || !userByUid.isPresent()) {
            throw new RuntimeException(userNotFoundError);
        } else {

            validateUserForUpdate(userByUsername.get(), userByUid.get());

            log.info("User is valid for updating the details.");

            if (userRepository.findByUsername(changeUserUsernameRequest.getRequestedUsername().trim()).isPresent()) {

                throw new RuntimeException(duplicateUsernameError);
            } else {

                userByUsername.get().setUsername(changeUserUsernameRequest.getRequestedUsername().trim());

                userRepository.save(userByUsername.get());
                log.info("Changed user's username to " + changeUserUsernameRequest.getRequestedUsername());
            }
        }
        return userByUsername.orElseThrow(() -> new RuntimeException(userNotFoundError));
    }

    private void validateUserForUpdate(UserAccount user, UserAccount userByUid) throws RuntimeException {

        if (user != userByUid) {
            throw new RuntimeException(invalidUserAndUIDError);
        } else if (!user.isAccountVerified()) {
            throw new RuntimeException(accountNotVerifiedError);
        }
    }

    @Override
    public AccountVerificationMessage verifyAccount(String token) {

        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByConfirmationToken(token);

        if (confirmationToken.isPresent()) {

            Optional<UserAccount> user = userRepository.findByEmailId(confirmationToken.get().getUserEmailId());

            if (user.isPresent()) {

                if (user.get().isAccountVerified()) {

                    return AccountVerificationMessage.ALREADY_VERIFIED;
                }

                user.get().setAccountVerified(true);
                userRepository.save(user.get());
                confirmationTokenRepository.delete(confirmationToken.get());

                return AccountVerificationMessage.VERIFIED;
            } else {
                return AccountVerificationMessage.INVALID;
            }
        }

        return AccountVerificationMessage.INVALID;
    }

    @Override
    public void updateUserPassword(String uid, String password) throws RuntimeException {

        UserAccount user = getUserByUid(uid);

        if (user != null) {
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setResetPasswordToken(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException(userNotFoundError);
        }
    }

    @Override
    public void updateUserResetPasswordToken(String uid, String resetPasswordToken) throws RuntimeException {

        UserAccount user = getUserByUid(uid);

        if (user != null) {

            log.info("Reset password token : " + resetPasswordToken);
            user.setResetPasswordToken(resetPasswordToken);
            userRepository.save(user);
        } else {
            throw new RuntimeException(userNotFoundError);
        }
    }

    @Override
    public void addRoleToTheUser(UserAccount user, String roleName) {

        Optional<Role> role = roleRepository.findByRoleName(roleName);
        role.ifPresent(value -> user.getRoles().add(value));
    }

    @Override
    public UserAccount getUserByUserName(String username) {

        Optional<UserAccount> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    @Override
    public UserAccount getUserByEmailId(String emailId) {

        Optional<UserAccount> user = userRepository.findByEmailId(emailId);
        return user.orElse(null);
    }

    @Override
    public UserAccount getUserByUid(String uid) {
        Optional<UserAccount> user = userRepository.findByUid(uid);
        return user.orElse(null);
    }

}
