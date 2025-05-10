package com.mumble.app;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class InputSanitiser {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
                                                .allowElements("b", "i", "u", "em", "strong", "br") // allow simple formatting
                                                .allowUrlProtocols("https")
                                                .toFactory();

    public static String sanitniseHtml(String input){
        if(input == null) return "";
        return POLICY.sanitize(input);
    }
    
    public static String sanitiseString(String message){
        if(message == null) return "";

        // remove html tags
        message = message.replaceAll("<[^>]*>", "");

        // escape dangerous characters
        message = message.replace("&", "&amp");
        message = message.replace("<", "&lt");
        message = message.replace(">", "&gt");

        // trim whitespace
        message = message.trim();

        // enforce max length
        if(message.length() > 500){
            message = message.substring(0, 500);
        }

        return message;
    }

    public static String sanitiseUsername(String input){

        if(input == null) return "";

        // onlt allow letters digits and underscores
        return input.trim().replaceAll("[^a-zA-Z0-9_]", "");
    }

    
}
