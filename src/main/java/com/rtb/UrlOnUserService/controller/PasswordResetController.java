package com.rtb.UrlOnUserService.controller;

import com.rtb.UrlOnUserService.domain.UserAccount;
import com.rtb.UrlOnUserService.service.EmailService;
import com.rtb.UrlOnUserService.service.UserService;
import com.rtb.UrlOnUserService.util.JWT_Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static com.rtb.UrlOnUserService.constantsAndEnums.Constants.*;
import static com.rtb.UrlOnUserService.constantsAndEnums.ErrorMessage.*;

@Controller
@RequestMapping("/urlon/api/users")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/account/forgotPassword")
    public String forgotPasswordForm() {

        return "forgot-password-form";
    }

    @PostMapping("/account/forgotPassword")
    public String forgotPasswordFormSubmit(HttpServletRequest request, Model model) {

        UserAccount user = userService.getUserByEmailId(request.getParameter("email"));

        if (user == null) {

            model.addAttribute(ERROR, userNotFoundError);
        } else if (!user.isAccountVerified()) {

            model.addAttribute(ERROR, accountNotVerifiedError_forgotPassword);
        } else {

            String token = JWT_Util.generateTokenWithExpiry(user.getEmailId(), System.currentTimeMillis() + 10 * 60 * 1000);

            String resetPasswordUrl = "http://localhost:8004" + "/urlon/api/users/account/passwordReset?uid=" + user.getUid() + "&token=" + token;

            try {
                emailService.sendPasswordResetUrl(user, resetPasswordUrl);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute(ERROR, sendingMailError);
                return "forgot-password-form";
            }

            userService.updateUserResetPasswordToken(user.getUid(), token);
            model.addAttribute(MESSAGE, RESET_PASSWORD_MESSAGE);
        }

        return "forgot-password-form";
    }

    @GetMapping("/account/passwordReset")
    public String passwordResetForm(@RequestParam(name = "uid") String uid, @RequestParam("token") String token, Model model) {

        UserAccount user = userService.getUserByUid(uid);

        if (user == null || !JWT_Util.isTokenValid(token) || user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(token)) {

            model.addAttribute(MESSAGE, linkExpiredOrInvalidError);
            return "reset-password-message";
        }

        if (!user.isAccountVerified()) {
            model.addAttribute(MESSAGE, accountNotVerifiedError_forgotPassword);
            return "reset-password-message";
        }

        model.addAttribute("token", token);
        model.addAttribute("uid", user.getUid());
        return "reset-password-form";
    }

    @PostMapping("/account/passwordReset")
    public String passwordResetFormSubmit(HttpServletRequest request, Model model) {

        String token = request.getParameter("token");
        UserAccount user = userService.getUserByResetPasswordToken(token);

        if (user == null || !JWT_Util.isTokenValid(token)) {

            model.addAttribute(MESSAGE, linkExpiredOrInvalidError);
            return "reset-password-message";
        }

        String password = request.getParameter("password");
        String uid = request.getParameter("uid");

        // update password
        userService.updateUserPassword(uid, password);

        model.addAttribute(MESSAGE, PASSWORD_RESET_SUCCESSFUL);
        return "reset-password-message";
    }

}
