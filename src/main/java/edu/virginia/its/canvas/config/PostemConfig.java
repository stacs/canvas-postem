package edu.virginia.its.canvas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PostemConfig {

  @Value("${ltitool.canvas.apiUrl}")
  private String apiUrl;

  @Value("${ltitool.canvas.apiToken}")
  private String oauthToken;

  @Bean
  public WebClient webClient() {
    String toolName = this.getClass().getPackage().getImplementationTitle();
    String toolVersion = this.getClass().getPackage().getImplementationVersion();
    return WebClient.builder()
        .baseUrl(apiUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oauthToken)
        .defaultHeader(HttpHeaders.USER_AGENT, toolName + ":" + toolVersion)
        .build();
  }
}
