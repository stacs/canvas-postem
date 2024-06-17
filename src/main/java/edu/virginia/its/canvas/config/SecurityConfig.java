package edu.virginia.its.canvas.config;

import edu.virginia.its.canvas.util.CanvasRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

@EnableWebSecurity
public class SecurityConfig {

  @Autowired public CanvasRoleMapper canvasRoleMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(
            "/error",
            "/resources/**",
            "/favicon.ico",
            "/config.json",
            "/.well-known/jwks.json",
            "/lti/login")
        .permitAll()
        .antMatchers("/instructorHome")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/viewFile")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/studentView")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/studentFileInfo")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/editFile")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/upload")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/uploadNewVersion")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/delete")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/release")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/unrelease")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/downloadFile")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/downloadCSV")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/renameFile")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'TA', 'ADMIN')")
        .antMatchers("/**")
        .access(
            "!hasAnyRole('OBSERVER', 'LIBRARIAN', 'DESIGNER') and hasAnyRole('INSTRUCTOR', 'STUDENT', 'TA', 'ADMIN')");
    Lti13Configurer lti13Configurer =
        new Lti13Configurer().grantedAuthoritiesMapper(canvasRoleMapper);
    http.apply(lti13Configurer);

    return http.build();
  }
}
