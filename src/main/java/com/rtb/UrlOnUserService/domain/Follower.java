package com.rtb.UrlOnUserService.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "follower")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Follower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 50)
    private String userUid;
    @Column(name = "follower_uid", nullable = false, length = 50)
    private String followerUid;

    @Column(name = "followed_on", nullable = false)
    private Long followedOn;
}
