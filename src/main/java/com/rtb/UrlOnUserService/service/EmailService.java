package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.domain.UserAccount;
import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendConfirmationToken(UserAccount user) throws Exception;

    SimpleMailMessage sendPasswordResetUrl(UserAccount user, String resetPasswordUrl) throws Exception;

    void sendMail(SimpleMailMessage mailMessage) throws Exception;
}
