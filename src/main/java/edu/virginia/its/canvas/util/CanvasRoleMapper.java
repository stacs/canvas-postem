package edu.virginia.its.canvas.util;

import com.nimbusds.jose.shaded.json.JSONObject;
import edu.virginia.its.canvas.lti.util.RolesMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.ac.ox.ctl.lti13.lti.Claims;

/** Custom role mapper to work with canvas roles */
@Slf4j
@Component
public class CanvasRoleMapper implements GrantedAuthoritiesMapper {

  private final RolesMap roleMappings;

  public CanvasRoleMapper(RolesMap roleMappings) {
    this.roleMappings = roleMappings;
    log.info("Using the following role mappings: {}", roleMappings);
  }

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    Set<GrantedAuthority> newAuthorities = new HashSet<>(authorities);
    for (GrantedAuthority authority : authorities) {
      OidcUserAuthority userAuth = (OidcUserAuthority) authority;
      Object customClaims = userAuth.getAttributes().get(Claims.CUSTOM);
      if (customClaims instanceof JSONObject customJson) {
        String enrollmentRoles = customJson.getAsString(Constants.CANVAS_ROLES);
        if (!ObjectUtils.isEmpty(enrollmentRoles)) {
          String[] splitEnrollmentRoles = enrollmentRoles.split(",");
          for (String splitEnrollmentRole : splitEnrollmentRoles) {
            String newRole = roleMappings.get(splitEnrollmentRole);
            if (newRole != null) {
              newAuthorities.add(
                  new OidcUserAuthority(newRole, userAuth.getIdToken(), userAuth.getUserInfo()));
            }
          }
        }
      }
    }
    return newAuthorities;
  }
}
