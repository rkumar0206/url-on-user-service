package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendConfirmationToken(UrlOnUser user) throws RuntimeException;

    void sendPasswordResetUrl(UrlOnUser user, String resetPasswordUrl) throws RuntimeException;

    void sendMail(SimpleMailMessage mailMessage) throws RuntimeException;
}
