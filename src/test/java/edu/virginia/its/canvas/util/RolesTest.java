package edu.virginia.its.canvas.util;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.shaded.json.JSONObject;
import edu.virginia.its.canvas.PostemApplication;
import edu.virginia.its.canvas.config.SecurityConfig;
import edu.virginia.its.canvas.lti.util.CanvasAuthenticationToken;
import edu.virginia.its.canvas.service.impl.CanvasFileService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {PostemApplication.class, SecurityConfig.class})
@TestPropertySource(properties = {"ltitool.canvas.apiUrl=localhost"})
public class RolesTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CanvasFileService canvasFileService;

  @Test
  void noAuth() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/config.json")).andExpect(status().isOk());
  }

  @Test
  public void testAllowedFeatures_teacher() throws Exception {
    List<String> roles = new ArrayList<>();
    roles.add("ROLE_INSTRUCTOR");
    SecurityContextHolder.getContext()
        .setAuthentication(getToken(roles, "user", "test@example.com", "123", "TeacherEnrollment"));
    when(canvasFileService.listFiles("123", "tz")).thenReturn(new ArrayList<>());

    this.mockMvc.perform(get("/instructorHome")).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ROLE_STUDENT"})
  void testForbiddenFeatures_student(String role) throws Exception {
    List<String> roles = new ArrayList<>();
    roles.add(role);
    SecurityContextHolder.getContext()
        .setAuthentication(getToken(roles, "test", "test@example.com", "123", "StudentEnrollment"));
    this.mockMvc.perform(get("/instructorHome")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/viewFile")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/studentView")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/studentFileInfo")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/editFile")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/upload")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/uploadNewVersion")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/delete")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/release")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/unrelease")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/downloadFile")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/downloadCSV")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/renameFile")).andExpect(status().isForbidden());
    this.mockMvc.perform(get("/config.json")).andExpect(status().isOk());
  }

  private CanvasAuthenticationToken getToken(
      List<String> roles,
      String username,
      String email,
      String courseId,
      String customCanvasRoles) {
    String nameAttributeKey = "sub";
    Map<String, Object> attributes =
        getAttributes(nameAttributeKey, username, email, courseId, customCanvasRoles);
    OAuth2User user =
        new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"), attributes, nameAttributeKey);
    roles.add("ROLE_USER");

    OidcAuthenticationToken oidcAuthenticationToken =
        new OidcAuthenticationToken(
            user,
            AuthorityUtils.createAuthorityList(roles.toArray(String[]::new)),
            "authorizedClientRegistrationId",
            "state");
    return new CanvasAuthenticationToken(oidcAuthenticationToken);
  }

  private Map<String, Object> getAttributes(
      String nameAttributeKey,
      String username,
      String email,
      String courseId,
      String customCanvasRoles) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(nameAttributeKey, Objects.requireNonNullElse(username, "username"));
    attributes.put("https://www.instructure.com/placement", "myPlacement");
    attributes.put("email", email);
    attributes.put("name", "myName");
    attributes.put("given_name", "myGivenName");
    attributes.put("family_name", "myFamilyName");
    attributes.put("picture", "myPicture");
    attributes.put("locale", "en");
    JSONObject customFields = new JSONObject();
    if (courseId != null) {
      customFields.put(Constants.CANVAS_COURSE_ID, courseId);
    }
    if (username != null) {
      customFields.put(Constants.CANVAS_USER_ID, username);
    }
    if (customCanvasRoles != null) {
      customFields.put(Constants.CANVAS_ROLES, customCanvasRoles);
    }
    if (!customFields.isEmpty()) {
      attributes.put(Claims.CUSTOM, customFields);
    }
    return attributes;
  }
}
