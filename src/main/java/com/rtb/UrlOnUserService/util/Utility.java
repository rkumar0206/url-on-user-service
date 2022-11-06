package com.rtb.UrlOnUserService.util;

import javax.servlet.http.HttpServletRequest;
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

}
