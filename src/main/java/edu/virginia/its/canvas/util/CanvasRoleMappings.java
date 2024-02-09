package edu.virginia.its.canvas.util;

import edu.virginia.its.canvas.lti.util.RolesMap;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CanvasRoleMappings {

  @Bean
  public RolesMap roleMappings() {
    RolesMap roleMappings = new RolesMap();
    roleMappings.put(
        Constants.TEACHER_ENROLLMENT, edu.virginia.its.canvas.lti.util.Constants.INSTRUCTOR_ROLE);
    roleMappings.put(Constants.TA_ENROLLMENT, edu.virginia.its.canvas.lti.util.Constants.TA_ROLE);
    roleMappings.put(
        Constants.ACCOUNT_ADMIN, edu.virginia.its.canvas.lti.util.Constants.ADMIN_ROLE);
    roleMappings.put(
        Constants.SUBACCOUNT_ADMIN, edu.virginia.its.canvas.lti.util.Constants.ADMIN_ROLE);
    roleMappings.put(
        Constants.STUDENT_ENROLLMENT, edu.virginia.its.canvas.lti.util.Constants.STUDENT_ROLE);
    roleMappings.put(
        Constants.WAITLISTED_STUDENT, edu.virginia.its.canvas.lti.util.Constants.STUDENT_ROLE);
    roleMappings.put(Constants.LIBRARIAN, "ROLE_LIBRARIAN");
    roleMappings.put(Constants.DESIGNER_ENROLLMENT, "ROLE_DESIGNER");
    roleMappings.put(Constants.OBSERVER_ENROLLMENT, "ROLE_OBSERVER");
    return roleMappings;
  }
}
