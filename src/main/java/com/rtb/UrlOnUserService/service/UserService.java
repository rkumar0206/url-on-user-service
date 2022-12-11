package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.RoleNames;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.models.*;
import org.springframework.data.domain.Page;

public interface UserService {

    UserAccount saveUser(UserCreateRequest userCreateRequest);

    UserAccount updateUserDetails(UpdateUserDetailsRequest updateUserDetailsRequest);

    UserAccount changeUserEmailId(ChangeUserEmailIdRequest changeUserEmailIdRequest);

    UserAccount changeUserUsername(ChangeUserUsernameRequest changeUserUsernameRequest);

    void addFollower(AddOrDeleteFollowerRequest addOrDeleteFollowerRequest);

    void deleteFollower(AddOrDeleteFollowerRequest addOrDeleteFollowerRequest);

    Page<UserAccount> getAllFollowersOfUser(String uid);

    void addRoleToTheUser(UserAccount user, RoleNames roleName);

    UserAccount getUserByUserName(String username);

    UserAccount getUserByEmailId(String emailId);

    UserAccount getUserByUid(String uid);

    UserAccount getUserByEmailIdOrByUsername(String username);

    UserAccount getUserByResetPasswordToken(String resetPasswordUrl);

    AccountVerificationMessage verifyAccount(String token);

    void updateUserPassword(String uid, String password) throws RuntimeException;

    void updateUserResetPasswordToken(String uid, String resetPasswordToken) throws RuntimeException;
}
