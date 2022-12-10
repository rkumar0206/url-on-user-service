package com.rtb.UrlOnUserService.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "follower")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uid;
    private Long followedOn;

    public Follower(String uid, Long followedOn) {
        this.uid = uid;
        this.followedOn = followedOn;
    }

}
