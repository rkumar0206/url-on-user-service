package com.rtb.UrlOnUserService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDetailsRequest {

    private String emailId;
    private String uid;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImage;
    private Date dob;


    @JsonIgnore
    public boolean isUserDetailsValidForUpdate() {

        return emailId != null && !emailId.trim().equals("")
                && uid != null && !uid.trim().equals("")
                && firstName != null && !firstName.trim().equals("")
                && dob != null;
    }
}
