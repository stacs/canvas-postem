package edu.virginia.its.canvas.config;

import edu.virginia.its.canvas.util.CanvasRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired public CanvasRoleMapper canvasRoleMapper;

  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http, ApplicationContext context)
      throws Exception {
    String studentAccessString =
        "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'STUDENT', 'TA', 'ADMIN')";
    String noStudentAccessString =
        "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')";
    DefaultHttpSecurityExpressionHandler expressionHandler =
        new DefaultHttpSecurityExpressionHandler();
    expressionHandler.setApplicationContext(context);
    WebExpressionAuthorizationManager studentAccess =
        new WebExpressionAuthorizationManager(studentAccessString);
    WebExpressionAuthorizationManager noStudentAccess =
        new WebExpressionAuthorizationManager(noStudentAccessString);
    studentAccess.setExpressionHandler(expressionHandler);
    noStudentAccess.setExpressionHandler(expressionHandler);
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers(
                    "/error",
                    "/resources/**",
                    "/favicon.ico",
                    "/config.json",
                    "/.well-known/jwks.json",
                    "/lti/login")
                .permitAll()
                .requestMatchers(
                    "/instructorHome",
                    "/viewFile",
                    "/studentView",
                    "/studentFileInfo",
                    "/editFile",
                    "/upload",
                    "/uploadNewVersion",
                    "/delete",
                    "/release",
                    "/unrelease",
                    "/downloadFile",
                    "/downloadCSV",
                    "/renameFile")
                .access(noStudentAccess)
                .requestMatchers("/**")
                .access(studentAccess));
    Lti13Configurer lti13Configurer =
        new Lti13Configurer().grantedAuthoritiesMapper(canvasRoleMapper);
    lti13Configurer.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
    http.with(lti13Configurer, Customizer.withDefaults());
    return http.build();
  }
}
