package com.rtb.UrlOnUserService.models;

import com.rtb.UrlOnUserService.domain.Role;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSelfDetailsResponse implements Serializable {

    private String emailId;
    private String username;
    private String uid;
    private String firstName;
    private String lastName;
    private String profileImage;
    private String phoneNumber;
    private Date dob;
    private Collection<Role> roles = new ArrayList<>();
}
