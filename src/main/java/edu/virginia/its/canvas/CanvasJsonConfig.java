package edu.virginia.its.canvas;

import edu.virginia.its.canvas.util.Constants;
import edu.virginia.lts.canvas.Config;
import edu.virginia.lts.canvas.Extension;
import edu.virginia.lts.canvas.Placement;
import edu.virginia.lts.canvas.Settings;
import edu.virginia.lts.canvas.placements.CourseNavigation;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanvasJsonConfig {

  @Value("${ltitool.baseUrl}")
  private String baseUrl;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Bean
  public Config getConfig() throws MalformedURLException {

    String toolName = this.getClass().getPackage().getImplementationTitle();
    String domain = new URL(baseUrl).getHost();
    String appUrl = baseUrl + contextPath;

    CourseNavigation courseNavigation =
        CourseNavigation.builder()
            .placement("course_navigation")
            .messageType(Placement.LTI_RESOURCE_LINK_REQUEST)
            .text("Posted Feedback")
            .visibility("members")
            .defaultString("hidden")
            .customField(Constants.CANVAS_COURSE_ID, "$Canvas.course.id")
            .customField(Constants.CANVAS_USER_ID, "$Canvas.user.id")
            .customField(Constants.CANVAS_ROLES, "$Canvas.membership.roles")
            .customField(Constants.CANVAS_TIMEZONE, "$Person.address.timezone")
            .customField(Constants.CANVAS_USER, "$Canvas.user.loginId")
            .build();
    Settings settings = Settings.builder().placement(courseNavigation).build();
    Extension extension =
        Extension.builder()
            .domain(domain)
            .toolId(toolName)
            .platform(Extension.CANVAS_PLATFORM)
            .privacyLevel(Extension.PUBLIC)
            .settings(settings)
            .build();
    return Config.builder()
        .title("Posted Feedback")
        .description("Posted Feedback")
        .oidcInitiationUrl(appUrl + "/lti/login_initiation/" + toolName)
        .targetLinkUri(appUrl + "/launch")
        .scope(Config.LINEITEM)
        .scope(Config.RESULT_READONLY)
        .extension(extension)
        .publicJwkUrl(appUrl + "/.well-known/jwks.json")
        .build();
  }
}
