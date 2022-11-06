package com.rtb.UrlOnUserService.controller;

import com.rtb.UrlOnUserService.domain.UrlOnUser;
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

        UrlOnUser user = userService.getUserByEmailId(request.getParameter("email"));

        if (user == null) {

            model.addAttribute("error", "User Invalid");
        } else {

            emailService.sendPasswordResetUrl(user);
            model.addAttribute("message", "Please check you email for password reset url. The link will expire in 10 minutes");
        }

        return "forgot-password-form";
    }

    @GetMapping("/account/passwordReset")
    public String passwordResetForm(@RequestParam(name = "uid") String uid, @RequestParam("token") String token, Model model) {

        UrlOnUser user = userService.getUserByUid(uid);

        if (user == null || !JWT_Util.isTokenValid(token)) {

            model.addAttribute("message", "Link invalid");
            return "reset-password-message";
        }

        model.addAttribute("token", token);
        model.addAttribute("uid", user.getUid());
        return "reset-password-form";
    }

    @PostMapping("/account/passwordReset")
    public String passwordResetFormSubmit(HttpServletRequest request, Model model) {

        String token = request.getParameter("token");

        if (!JWT_Util.isTokenValid(token)) {

            model.addAttribute("message", "Link expired");
            return "reset-password-message";
        }

        String password = request.getParameter("password");
        String uid = request.getParameter("uid");

        // update password
        userService.updateUserPassword(uid, password);

        model.addAttribute("message", "Password reset successful");
        return "reset-password-message";
    }

}
