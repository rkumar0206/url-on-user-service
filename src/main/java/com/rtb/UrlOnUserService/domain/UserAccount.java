package com.rtb.UrlOnUserService.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_account")
@Getter
@Setter
@ToString
@AllArgsConstructor
public class UserAccount implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_id", nullable = false, unique = true, length = 50)
    private String emailId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "uid", nullable = false, unique = true, length = 50)
    private String uid;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    private String profileImage;

    private String phoneNumber;

    @Column(name = "dob", nullable = false)
    private Date dob;

    @Column(name = "is_account_verified")
    private boolean isAccountVerified = false;

    private String resetPasswordToken;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    public UserAccount() {
        this.roles = new ArrayList<>();
    }

    public UserAccount(String emailId, String username, String password, String uid, String firstName, String lastName, String profileImage, String phoneNumber, Date dob, boolean isAccountVerified, String resetPasswordToken, List<Role> roles) {
        this.emailId = emailId;
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.isAccountVerified = isAccountVerified;
        this.resetPasswordToken = resetPasswordToken;
        this.roles = roles;
    }

    public UserAccount(String emailId, String username, String password, String uid, String firstName, String lastName, String profileImage, String phoneNumber, Date dob, boolean isAccountVerified, String resetPasswordToken) {
        this();
        this.emailId = emailId;
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.isAccountVerified = isAccountVerified;
        this.resetPasswordToken = resetPasswordToken;
    }
}
