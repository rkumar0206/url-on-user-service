package com.rtb.UrlOnUserService.util;

import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.models.UserDetailResponse;

public class ModelMapper {

    public static UserDetailResponse buildUserDetailResponse(UserAccount user) {

        return UserDetailResponse.builder()
                .emailId(user.getEmailId())
                .username(user.getUsername())
                .uid(user.getUid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .roles(user.getRoles())
                .build();

    }
}
