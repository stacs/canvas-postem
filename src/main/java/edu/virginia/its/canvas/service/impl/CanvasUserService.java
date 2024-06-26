package edu.virginia.its.canvas.service.impl;

import edu.virginia.its.canvas.model.CanvasData;
import edu.virginia.its.canvas.util.Constants;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class CanvasUserService {

  @Autowired private WebClient restClient;

  @Value("${ltitool.canvas.apiToken}")
  private String oauthToken;

  public HashMap<String, String> getUsersOfCourse(String course_id) {

    HashMap<String, String> userDisplayNameMap = new HashMap<>();

    List<CanvasData.User> userList = new ArrayList<>();

    ResponseEntity<String> resp =
        restClient
            .get()
            .uri(
                UriComponentsBuilder.fromPath("/courses/" + course_id + "/users")
                    .queryParam("enrollment_type[]", "student")
                    .queryParam("enrollment_state[]", "active")
                    .queryParam("per_page", 100)
                    .buildAndExpand()
                    .toString())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .toEntity(String.class)
            .block();

    populateUsers(resp, userList);

    String nextPage = canvasNextPage(resp);
    while (nextPage != null) {

      resp =
          restClient
              .get()
              .uri(nextPage)
              .header("Authorization", "Bearer " + oauthToken)
              .retrieve()
              .toEntity(String.class)
              .block();

      populateUsers(resp, userList);
      nextPage = canvasNextPage(resp);
    }

    for (CanvasData.User user : userList) {
      userDisplayNameMap.put(user.login_id(), user.sortable_name());
    }

    return userDisplayNameMap;
  }

  private void populateUsers(ResponseEntity<String> resp, List<CanvasData.User> userList) {

    JSONArray respArr = new JSONArray(resp.getBody());
    if (respArr != null) {
      IntStream.range(0, respArr.length())
          .forEach(
              i -> {
                JSONObject jsonObj = respArr.getJSONObject(i);
                if (jsonObj != null) {
                  String sortableName = jsonObj.getString("sortable_name");
                  String[] splitName = sortableName.split(",");
                  String login_id = jsonObj.has("login_id") ? jsonObj.getString("login_id") : "";
                  String[] userDetails = {login_id, splitName[0], ""};
                  if (splitName.length == 2) {
                    userDetails[1] = splitName[1];
                  }
                  userList.add(
                      new CanvasData.User(
                          jsonObj.getLong("id"), jsonObj.getString("sortable_name"), login_id));
                }
              });
    }
  }

  private String canvasNextPage(ResponseEntity<String> resp) {
    String linkHeader = resp.getHeaders().getFirst(Constants.CANVAS_LINK_HEADER);
    String nextLink = null;
    if (linkHeader != null && !linkHeader.isEmpty()) {
      String[] links = linkHeader.split(",");
      for (String link : links) {
        Link link1 = Link.valueOf(link);
        if (link1.hasRel("next")) {
          String href = link1.getHref();
          nextLink = URLDecoder.decode(href, StandardCharsets.UTF_8);
          break;
        }
      }
    }
    return nextLink;
  }
}
