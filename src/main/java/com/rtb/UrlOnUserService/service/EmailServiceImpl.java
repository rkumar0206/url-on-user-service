package com.rtb.UrlOnUserService.service;

import com.rtb.UrlOnUserService.constantsAndEnums.Constants;
import com.rtb.UrlOnUserService.domain.ConfirmationToken;
import com.rtb.UrlOnUserService.domain.UrlOnUser;
import com.rtb.UrlOnUserService.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final JavaMailSender javaMailSender;

    @Override
    public void sendConfirmationToken(UrlOnUser user) {

        log.info("Creating confirmation toke url");

        ConfirmationToken confirmationToken = new ConfirmationToken(user.getEmailId());

        if (confirmationTokenRepository.findByUserEmailId(user.getEmailId()).isPresent()) {

            confirmationToken = confirmationTokenRepository.findByUserEmailId(user.getEmailId()).get();
        } else {

            confirmationTokenRepository.save(confirmationToken);
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmailId());
        simpleMailMessage.setSubject(Constants.CONFIRMATION_EMAIL_SUBJECT);
        simpleMailMessage.setFrom(Constants.EMAIL_FROM);
        simpleMailMessage.setText("To verify your account from UrlOn application please click on below link\n" +
                Constants.CONFIRMATION_EMAIL_BASE_URL + confirmationToken.getConfirmationToken());

        sendMail(simpleMailMessage);
    }

    @Override
    public void sendPasswordResetUrl(UrlOnUser user) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmailId());
        simpleMailMessage.setSubject(Constants.PASSWORD_RESET_EMAIL_SUBJECT);
        simpleMailMessage.setFrom(Constants.EMAIL_FROM);
        simpleMailMessage.setText("To reset your password please follow the below link\n" +
                Constants.PASSWORD_RESET_EMAIL_BASE_URL + user.getUid());
        sendMail(simpleMailMessage);
    }

    @Override
    public void sendMail(SimpleMailMessage mailMessage) {

        try {

            log.info("Sending mail...");

            javaMailSender.send(mailMessage);

            log.info("Mail sent successfully...");

        } catch (Exception e) {
            e.printStackTrace();
            log.info("Mail not sent!!");
        }
    }
}
