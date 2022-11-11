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

        UrlOnUser user = getUserByEmailIdOrByUsername(username);

        if (user == null) {

            throw new UsernameNotFoundException(userNotFoundError);
        } else if (!user.isAccountVerified()) {
            throw new RuntimeException(accountNotVerified);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getRoleName())));

        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public UrlOnUser getUserByEmailIdOrByUsername(String username) {

        if (Utility.isValidEmailAddress(username)) {

            return getUserByEmailId(username);
        } else {

            return getUserByUserName(username);
        }
    }

    @Override
    public UrlOnUser getUserByResetPasswordToken(String resetPasswordUrl) {

        return userRepository.findByResetPasswordToken(resetPasswordUrl).orElse(null);
    }

    @Override
    public UrlOnUser saveUser(UserRequest userRequest) {

        UrlOnUser user;
        Optional<UrlOnUser> tempUser = userRepository.findByEmailId(userRequest.getEmailId());

        if (tempUser.isPresent() && !tempUser.get().isAccountVerified()) {

            user = tempUser.get();

            user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));

            if (!user.getUsername().trim().equals(userRequest.getUsername().trim())) {

                if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                    throw new RuntimeException(duplicateUsernameError);
                }
            }

            user.setDob(userRequest.getDob());
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setPhoneNumber(userRequest.getPhoneNumber());
            user.setProfileImage(userRequest.getProfileImage());
            user.setUsername(userRequest.getUsername());

            userRepository.save(user);

            try {
                emailService.sendConfirmationToken(user);
            } catch (Exception exception) {
                log.error(sendingMailError, exception);
                throw new RuntimeException(sendingMailError);
            }
            log.info("Confirmation token sent");
        } else {

            user = objectMapper.convertValue(userRequest, UrlOnUser.class);

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
    public UrlOnUser updateUserDetails(UserRequest userRequest) {
        return null;
    }

    @Override
    public AccountVerificationMessage verifyAccount(String token) {

        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByConfirmationToken(token);

        if (confirmationToken.isPresent()) {

            Optional<UrlOnUser> user = userRepository.findByEmailId(confirmationToken.get().getUserEmailId());

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

        UrlOnUser user = getUserByUid(uid);

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

        UrlOnUser user = getUserByUid(uid);

        if (user != null) {

            log.info("Reset password token : " + resetPasswordToken);
            user.setResetPasswordToken(resetPasswordToken);
            userRepository.save(user);
        } else {
            throw new RuntimeException(userNotFoundError);
        }
    }

    @Override
    public void addRoleToTheUser(UrlOnUser user, String roleName) {

        Optional<Role> role = roleRepository.findByRoleName(roleName);
        role.ifPresent(value -> user.getRoles().add(value));
    }

    @Override
    public UrlOnUser getUserByUserName(String username) {

        Optional<UrlOnUser> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    @Override
    public UrlOnUser getUserByEmailId(String emailId) {

        Optional<UrlOnUser> user = userRepository.findByEmailId(emailId);
        return user.orElse(null);
    }

    @Override
    public UrlOnUser getUserByUid(String uid) {
        Optional<UrlOnUser> user = userRepository.findByUid(uid);
        return user.orElse(null);
    }

}
