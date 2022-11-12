package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

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

        return emailId != null && !emailId.trim().equals("")
                && username != null && !username.trim().equals("")
                && firstName != null && !firstName.trim().equals("")
                && password != null && !password.trim().equals("")
                && dob != null;
    }

    @JsonIgnore
    public boolean isUserDetailsValidForUpdate() {

        return emailId != null && !emailId.trim().equals("")
                && firstName != null && !firstName.trim().equals("")
                && dob != null;
    }

}
