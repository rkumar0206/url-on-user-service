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

    @Column(name = "following_uid", nullable = false, length = 50)
    private String followingUid;
    @Column(name = "follower_uid", nullable = false, length = 50)
    private String followerUid;

    @Column(name = "followed_on", nullable = false)
    private Long followedOn;

    public Follower(String followingUid, String followerUid, Long followedOn) {
        this.followingUid = followingUid;
        this.followerUid = followerUid;
        this.followedOn = followedOn;
    }

    public Follower(String followingUid, String followerUid) {
        this.followingUid = followingUid;
        this.followerUid = followerUid;
        this.followedOn = System.currentTimeMillis();
    }
}
