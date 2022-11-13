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

        return StringUtils.hasLength(emailId.trim())
                && StringUtils.hasLength(uid.trim())
                && StringUtils.hasLength(firstName.trim())
                && dob != null;
    }
}
