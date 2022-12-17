package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserAccount saveUser(UserCreateRequest userCreateRequest);

    UserAccount updateUserDetails(UpdateUserDetailsRequest updateUserDetailsRequest);

    UserAccount changeUserEmailId(ChangeUserEmailIdRequest changeUserEmailIdRequest);

    UserAccount changeUserUsername(ChangeUserUsernameRequest changeUserUsernameRequest);

    void followUser(FollowAndUnfollowRequest followAndUnfollowRequest);

    void unfollowUser(FollowAndUnfollowRequest followAndUnfollowRequest);

    Page<UserAccount> getAllFollowersOfUser(String uid, Pageable pageable);

    Page<UserAccount> getAllFollowingsOfUser(String uid, Pageable pageable);

    void addRoleToTheUser(UserAccount user, String roleName);

    UserAccount getUserByUserName(String username);

    UserAccount getUserByEmailId(String emailId);

    UserAccount getUserByUid(String uid);

    UserAccount getUserByEmailIdOrByUsername(String username);

    UserAccount getUserByResetPasswordToken(String resetPasswordUrl);

    UserAccount getAuthenticatedUser();

    AccountVerificationMessage verifyAccount(String token);

    void updateUserPassword(String uid, String password) throws RuntimeException;

    void updateUserResetPasswordToken(String uid, String resetPasswordToken) throws RuntimeException;
}
