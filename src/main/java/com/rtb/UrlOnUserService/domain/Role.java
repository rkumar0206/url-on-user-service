package com.rtb.UrlOnUserService.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_role")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private RoleNames roleName;
}
