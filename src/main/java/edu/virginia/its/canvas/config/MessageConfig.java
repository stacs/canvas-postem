package edu.virginia.its.canvas.config;

import edu.virginia.its.canvas.lti.util.CanvasTokenLocaleResolver;
import edu.virginia.its.canvas.lti.util.Constants;
import edu.virginia.its.canvas.lti.util.DatabaseMessageSource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class MessageConfig {

  @Value("${ltitool.messageSourcePropertiesFiles:/messages/messages.properties}")
  private List<String> messageSourcePropertiesFiles;

  @Bean
  public LocaleResolver localeResolver() {
    CanvasTokenLocaleResolver canvasTokenLocaleResolver = new CanvasTokenLocaleResolver();
    canvasTokenLocaleResolver.setDefaultLocale(Constants.DEFAULT_LOCALE);
    return canvasTokenLocaleResolver;
  }

  @Bean
  public MessageSource messageSource() {
    DatabaseMessageSource databaseMessageSource = new DatabaseMessageSource();
    if (messageSourcePropertiesFiles != null) {
      List<String> basenames = new ArrayList<>();
      for (String messageSourcePropertiesFile : messageSourcePropertiesFiles) {
        String messageSourceWithoutExtension =
            messageSourcePropertiesFile.substring(0, messageSourcePropertiesFile.lastIndexOf("."));
        basenames.add("classpath:" + messageSourceWithoutExtension);
      }
      databaseMessageSource.setBasenames(basenames.toArray(new String[] {}));
    }
    return databaseMessageSource;
  }
}
