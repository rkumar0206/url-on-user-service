package com.rtb.UrlOnUserService.util;

import com.rtb.UrlOnUserService.constantsAndEnums.Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utility {

    public static boolean isPatterMatches(String str, String regex) {

        return Pattern.compile(regex)
                .matcher(str)
                .matches();
    }

    public static boolean isValidEmailAddress(String emailAddress) {

        String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        return isPatterMatches(emailAddress, regexPattern);
    }

    public static String getSiteUrl(HttpServletRequest request) {

        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    public static List<String> getWhiteListedServletPaths() {

        List<String> whiteListedServletPaths = new ArrayList<>();

        whiteListedServletPaths.add("/urlon/app/users/login");
        whiteListedServletPaths.add("/urlon/api/users/token/refresh");
        whiteListedServletPaths.add("urlon/api/users/account/forgotPassword");
        whiteListedServletPaths.add("urlon/api/users/account/passwordReset");

        return whiteListedServletPaths;

    }

    public static String getTokenFromAuthorizationHeader(String authorizationHeader) {

        return authorizationHeader.substring(Constants.BEARER.length());
    }

}
