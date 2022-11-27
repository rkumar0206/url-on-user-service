package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.models.ChangeUserEmailIdRequest;
import com.rtb.UrlOnUserService.models.ChangeUserUsernameRequest;
import com.rtb.UrlOnUserService.models.UpdateUserDetailsRequest;
import com.rtb.UrlOnUserService.models.UserCreateRequest;

public interface UserService {

    UserAccount saveUser(UserCreateRequest userCreateRequest);

    UserAccount updateUserDetails(UpdateUserDetailsRequest updateUserDetailsRequest);

    UserAccount changeUserEmailId(ChangeUserEmailIdRequest changeUserEmailIdRequest);

    UserAccount changeUserUsername(ChangeUserUsernameRequest changeUserUsernameRequest);

    void addRoleToTheUser(UserAccount user, String roleName);

    UserAccount getUserByUserName(String username);

    UserAccount getUserByEmailId(String emailId);

    UserAccount getUserByUid(String uid);

    UserAccount getUserByEmailIdOrByUsername(String username);

    UserAccount getUserByResetPasswordToken(String resetPasswordUrl);

    AccountVerificationMessage verifyAccount(String token);

    void updateUserPassword(String uid, String password) throws RuntimeException;

    void updateUserResetPasswordToken(String uid, String resetPasswordToken) throws RuntimeException;
}
