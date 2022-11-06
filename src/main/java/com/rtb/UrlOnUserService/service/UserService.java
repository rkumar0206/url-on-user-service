package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.AccountVerificationMessage;
import com.rtb.UrlOnUserService.domain.Role;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.models.UserRequest;

public interface UserService {

    UrlOnUser saveUser(UserRequest userRequest);
    UrlOnUser updateUserDetails(UserRequest userRequest);

    void addRoleToTheUser(UrlOnUser user, String roleName);

    UrlOnUser getUserByUserName(String username);
    UrlOnUser getUserByEmailId(String emailId);
    UrlOnUser getUserByUid(String uid);

    UrlOnUser getUserByEmailIdOrByUsername(String username);

    AccountVerificationMessage verifyAccount(String token);
}
