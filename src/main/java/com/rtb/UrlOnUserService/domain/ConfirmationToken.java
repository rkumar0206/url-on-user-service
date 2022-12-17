package com.rtb.UrlOnUserService.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "confirmation_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private long tokenId;

    @Column(name = "confirmation_token", unique = true, nullable = false)
    private String confirmationToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "user_email_id", unique = true)
    private String userEmailId;

    public ConfirmationToken(String userEmailId) {

        this.userEmailId = userEmailId;
        this.confirmationToken = UUID.randomUUID().toString();
        createdDate = new Date();
    }
}
