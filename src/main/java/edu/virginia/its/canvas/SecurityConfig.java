package edu.virginia.its.canvas;

import edu.virginia.its.canvas.lti.util.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired private RoleMapper roleMapper;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.authorizeRequests()
        .antMatchers(
            "/resources/**", "/favicon.ico", "/config.json", "/.well-known/jwks.json", "/lti/login")
        .permitAll()
        .antMatchers("/**")
        .hasAnyRole("INSTRUCTOR", "TA", "ADMIN", "LEARNER");
    Lti13Configurer lti13Configurer = new Lti13Configurer().grantedAuthoritiesMapper(roleMapper);
    http.apply(lti13Configurer);
  }
}
