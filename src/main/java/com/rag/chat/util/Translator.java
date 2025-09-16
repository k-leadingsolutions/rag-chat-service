package com.rag.chat.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class Translator {

    private final MessageSource messageSource;

    public String toLocale(String msgCode, Locale locale) {
        return messageSource.getMessage(msgCode, null, locale != null ? locale : Locale.getDefault());
    }

    public static Locale getLocaleFromRequest(HttpServletRequest req) {
        Locale locale = req.getLocale();
        String lang = req.getHeader("Accept-Language");
        if (lang != null && !lang.isEmpty()) {
            return Locale.forLanguageTag(lang.split(",")[0]);
        }
        return locale != null ? locale : Locale.getDefault();
    }
}