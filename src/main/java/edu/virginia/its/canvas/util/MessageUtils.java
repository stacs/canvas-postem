package edu.virginia.its.canvas.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

@Component
public class MessageUtils {

  @Autowired private MessageSource messageSource;

  @Autowired private LocaleResolver localeResolver;

  public String getMessage(final String code, final Object... args) {
    // Canvas locale comes from the LTI token info, not the HTTP request, so we can pass a null
    // request to resolveLocale().
    return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(null));
  }
}
