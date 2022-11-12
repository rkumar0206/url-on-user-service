package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.models.ChangeUserEmailIdRequest;
import com.rtb.UrlOnUserService.models.UpdateUserDetailsRequest;
import com.rtb.UrlOnUserService.models.UserCreateRequest;

public interface UserService {

    UrlOnUser saveUser(UserCreateRequest userCreateRequest);

    UrlOnUser updateUserDetails(UpdateUserDetailsRequest updateUserDetailsRequest);

    UrlOnUser changeUserEmailId(ChangeUserEmailIdRequest changeUserEmailIdRequest);

    void addRoleToTheUser(UrlOnUser user, String roleName);

    UrlOnUser getUserByUserName(String username);

    UrlOnUser getUserByEmailId(String emailId);

    UrlOnUser getUserByUid(String uid);

    UrlOnUser getUserByEmailIdOrByUsername(String username);

    UrlOnUser getUserByResetPasswordToken(String resetPasswordUrl);

    AccountVerificationMessage verifyAccount(String token);

    void updateUserPassword(String uid, String password) throws RuntimeException;

    void updateUserResetPasswordToken(String uid, String resetPasswordToken) throws RuntimeException;
}
