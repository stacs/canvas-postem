package edu.virginia.its.canvas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PostemConfig {

  @Value("${ltitool.canvas.apiUrl}")
  private String apiUrl;

  @Bean
  public WebClient webClient() {
    return WebClient.builder().baseUrl("https://" + apiUrl).build();
  }
}
