package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequest {

    private String emailId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImage;
    private String password;
    private Date dob;

    @JsonIgnore
    public boolean isUserDetailsValidForCreate() {

        return StringUtils.hasLength(emailId.trim())
                && StringUtils.hasLength(username.trim())
                && StringUtils.hasLength(firstName.trim())
                && StringUtils.hasLength(password.trim())
                && dob != null;
    }

}
