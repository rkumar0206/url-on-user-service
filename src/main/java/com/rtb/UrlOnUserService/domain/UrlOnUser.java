package com.rtb.UrlOnUserService.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "UrlOnUser")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UrlOnUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private Collection<Role> roles = new ArrayList<>();
}
