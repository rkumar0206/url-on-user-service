package com.rtb.UrlOnUserService.models;

import lombok.*;

import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse implements Serializable {

    private String username;
    private String firstName;
    private String lastName;
    private String profileImage;
    private String phoneNumber;
    private Date dob;
}
