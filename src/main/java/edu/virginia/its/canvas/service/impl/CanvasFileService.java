package edu.virginia.its.canvas.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.opencsv.validators.RowMustHaveSameNumberOfColumnsAsFirstRowValidator;
import edu.virginia.its.canvas.model.CanvasData;
import edu.virginia.its.canvas.util.Constants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class CanvasFileService {

  @Autowired private WebClient restClient;

  @Value("${ltitool.canvas.apiToken}")
  private String oauthToken;

  private String notifyCanvas(
      String fileName, Boolean uploadAsLocked, String courseId, String userId) {

    long folderId = getFolderId(courseId);

    try {
      String resp =
          restClient
              .post()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/courses/" + courseId + "/files")
                          .queryParam("as_user_id", userId)
                          .queryParam("name", fileName)
                          .queryParam("parent_folder_id", folderId)
                          .queryParam("locked", uploadAsLocked)
                          .build())
              .header("Authorization", "Bearer " + oauthToken)
              .retrieve()
              .bodyToMono(String.class)
              .block();
      return resp;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    // UploadParams uploadParams = new UploadParams(uploadUrl: resp.json.upload_url, awsAccessKeyId:
    // resp.json.upload_params.AWSAccessKeyId,
    //        fileName: resp.json.upload_params.Filename, key: resp.json.upload_params.key, acl:
    // resp.json.upload_params.acl, policy: resp.json.upload_params.Policy,
    //        signature: resp.json.upload_params.Signature, successAccessRedirect:
    // resp.json.upload_params.success_action_redirect, contentType:
    // resp.json.upload_params.'content-type')
    return null;
  }

  private String awsUpload(String uploadParams, MultipartFile multipartFile) {

    JSONObject json = new JSONObject(uploadParams);

    String uploadUrl = json.getString("upload_url");
    JSONObject params = json.getJSONObject("upload_params");

    MultiValueMap<String, Object> form = new LinkedMultiValueMap();

    Iterator<String> keys = params.keys();

    while (keys.hasNext()) {
      String key = keys.next();
      form.add(key, params.getString(key));
    }

    LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
    headerMap.add(
        "Content-disposition",
        "form-data; name=file; filename=\"" + multipartFile.getOriginalFilename() + "\"");
    headerMap.add("Content-type", multipartFile.getContentType());

    String resp = "";
    try {
      HttpEntity<byte[]> doc = new HttpEntity<byte[]>(multipartFile.getBytes(), headerMap);
      form.add("file", doc);

      resp =
          restClient
              .post()
              .uri(uploadUrl)
              .header("Authorization", "Bearer " + oauthToken)
              .accept(MediaType.APPLICATION_JSON)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData(form))
              .retrieve()
              .bodyToMono(String.class)
              .block();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return resp;
  }

  public long upload(
      MultipartFile multipartFile,
      Boolean uploadAsLocked,
      Boolean releaseFeedback,
      String courseId,
      String fileTitle,
      String userId) {
    String uploadParams = notifyCanvas(fileTitle, uploadAsLocked, courseId, userId);

    JSONObject json = new JSONObject(uploadParams);
    if (json != null) {
      String awsUploadResponse = awsUpload(uploadParams, multipartFile);

      JSONObject uploadResponse = new JSONObject(awsUploadResponse);

      try {
        String resp =
            restClient
                .post()
                .uri(uploadResponse.getString("location"))
                .header("Authorization", "Bearer " + oauthToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JSONObject respObj = new JSONObject(resp);

        if (respObj.has("id")) {
          long fileId = respObj.getLong("id");
          log.info(
              " File Uploaded for Posted Feedback Tool by user "
                  + userId
                  + " in course "
                  + courseId
                  + ". File Details : display_name = "
                  + respObj.getString("display_name")
                  + ","
                  + " Id = "
                  + fileId
                  + ","
                  + " size = "
                  + respObj.getLong("size")
                  + ","
                  + "url = "
                  + respObj.getString("url"));

          if (releaseFeedback) {
            hideFile(String.valueOf(fileId), true);
          } else {
            hideFile(String.valueOf(fileId), false);
          }
          return fileId;

        } else {
          log.error(
              "File Upload failed by user "
                  + userId
                  + " in course "
                  + courseId
                  + ". File Title = "
                  + fileTitle);
          return -1;
        }

      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

    } else {
      log.error(
          "File Upload failed by user "
              + userId
              + " in course "
              + courseId
              + ". File Title = "
              + fileTitle);
    }
    return -1;
  }

  public List<CanvasData.File> listFiles(String courseId, String timeZone) {
    long folderId = getFolderId(courseId);
    if (folderId == -1) return new ArrayList<>();

    return fetchFiles(folderId, timeZone);
  }

  private long getFolderId(String courseId) {
    String folder = "Posted Feedback (Do NOT Publish)";
    final long[] folderId = new long[1];
    boolean folderNotFound = false;

    try {
      String result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/courses/" + courseId + "/folders/by_path/" + folder)
                          .build())
              .header("Authorization", "Bearer " + oauthToken)
              .retrieve()
              .bodyToMono(String.class)
              .block();

      JSONArray jsonArray = new JSONArray(result);
      IntStream.range(0, jsonArray.length())
          .forEach(
              i -> {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("name").equalsIgnoreCase(folder)) {
                  folderId[0] = jsonObject.getLong("id");
                }
              });

    } catch (Exception e) {
      if (e.getMessage().startsWith("404 Not Found")) {
        folderNotFound = true;
      }
    }

    if (folderNotFound) {
      String res =
          restClient
              .post()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/courses/" + courseId + "/folders")
                          .queryParam("name", folder)
                          .queryParam("locked", true)
                          .queryParam("parent_folder_path", "/")
                          .build())
              .header("Authorization", "Bearer " + oauthToken)
              .retrieve()
              .bodyToMono(String.class)
              .block();

      JSONObject jsonObj = new JSONObject(res);
      folderId[0] = jsonObj.getLong("id");
    }

    return folderId[0];
  }

  private List<CanvasData.File> fetchFiles(Long folderId, String timeZone) {
    List<CanvasData.File> fileList = new ArrayList<>();

    ResponseEntity<String> resp =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/folders/" + folderId + "/files")
                        .queryParam("include[]", "user")
                        .queryParam("sort", "created_at")
                        .queryParam("order", "desc")
                        .queryParam("per_page", 100)
                        .build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .toEntity(String.class)
            .block();

    populateFileList(resp, fileList, timeZone);

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

      populateFileList(resp, fileList, timeZone);
      nextPage = canvasNextPage(resp);
    }

    return fileList;
  }

  public CanvasData.File fetchFile(Long fileId, String timeZone) {

    String resp = null;
    try {

      resp =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder.path("/files/" + fileId).queryParam("include[]", "user").build())
              .header("Authorization", "Bearer " + oauthToken)
              .retrieve()
              .bodyToMono(String.class)
              .block();

    } catch (Exception e) {
      if (e.getMessage().startsWith("404 Not Found")) {
        log.error(e.getMessage(), e);
      }
    }
    if (resp != null) {

      JSONObject jsonObj = new JSONObject(resp);

      if (jsonObj != null) {
        String formattedFileName = jsonObj.getString("display_name");
        if (formattedFileName.contains(".csv")) {
          formattedFileName = formattedFileName.substring(0, formattedFileName.lastIndexOf('.'));
        }
        String modifiedBy =
            jsonObj.getJSONObject("user") != null
                    && jsonObj.getJSONObject("user").has("display_name")
                ? jsonObj.getJSONObject("user").getString("display_name")
                : "";

        String updatedAt = jsonObj.has("updated_at") ? jsonObj.getString("updated_at") : null;
        String updatedAtDateString = "";
        if (updatedAt != null) {
          OffsetDateTime updatedAtDate = OffsetDateTime.parse(updatedAt);
          updatedAtDateString =
              updatedAtDate
                  .toZonedDateTime()
                  .withZoneSameInstant(ZoneId.of(timeZone))
                  .toLocalDateTime()
                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        return new CanvasData.File(
            jsonObj.getLong("id"),
            formattedFileName,
            jsonObj.getString("filename"),
            jsonObj.getString("url"),
            jsonObj.getBoolean("locked"),
            jsonObj.getBoolean("hidden"),
            updatedAtDateString,
            modifiedBy);
      }
    }

    return null;
  }

  private static void populateFileList(
      ResponseEntity<String> resp, List<CanvasData.File> fileList, String timeZone) {

    if (resp != null) {

      JSONArray respArr = new JSONArray(resp.getBody());
      if (respArr != null) {
        IntStream.range(0, respArr.length())
            .forEach(
                i -> {
                  JSONObject jsonObj = respArr.getJSONObject(i);
                  if (jsonObj != null) {
                    String formattedFileName = jsonObj.getString("display_name");
                    if (formattedFileName.contains(".csv")) {
                      formattedFileName =
                          formattedFileName.substring(0, formattedFileName.lastIndexOf('.'));
                    }
                    String modifiedBy =
                        jsonObj.getJSONObject("user") != null
                                && jsonObj.getJSONObject("user").has("display_name")
                            ? jsonObj.getJSONObject("user").getString("display_name")
                            : "";

                    String updatedAt =
                        jsonObj.has("updated_at") ? jsonObj.getString("updated_at") : null;
                    String updatedAtDateString = "";
                    if (updatedAt != null) {
                      OffsetDateTime updatedAtDate = OffsetDateTime.parse(updatedAt);
                      updatedAtDateString =
                          updatedAtDate
                              .toZonedDateTime()
                              .withZoneSameInstant(ZoneId.of(timeZone))
                              .toLocalDateTime()
                              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }

                    fileList.add(
                        new CanvasData.File(
                            jsonObj.getLong("id"),
                            formattedFileName,
                            jsonObj.getString("filename"),
                            jsonObj.getString("url"),
                            jsonObj.getBoolean("locked"),
                            jsonObj.getBoolean("hidden"),
                            updatedAtDateString,
                            modifiedBy));
                  }
                });
      }
    }
  }

  public String deleteFile(String fileId) {

    String resp =
        restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/files/" + fileId).build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();

    String display_name = "";
    if (resp != null) {
      JSONObject jsonObj = new JSONObject(resp);
      if (jsonObj != null) {
        display_name = jsonObj.getString("display_name");
      }
      restClient
          .delete()
          .uri(uriBuilder -> uriBuilder.path("/files/" + fileId).build())
          .header("Authorization", "Bearer " + oauthToken)
          .retrieve()
          .bodyToMono(String.class)
          .block();
    }
    return display_name;
  }

  public void hideFile(String fileId, boolean hide) {

    String resp =
        restClient
            .put()
            .uri(
                uriBuilder ->
                    uriBuilder.path("/files/" + fileId).queryParam("hidden", hide).build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
  }

  public ArrayList<String> listUserLogins(String courseId) {
    ArrayList<String> users = new ArrayList<>();

    ResponseEntity<String> resp =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/courses/" + courseId + "/users")
                        .queryParam("enrollment_type[]", "student")
                        .queryParam("enrollment_state[]", "active")
                        .queryParam("per_page", 100)
                        .build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .toEntity(String.class)
            .block();

    populateUserLogins(resp, users);

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

      populateUserLogins(resp, users);
      nextPage = canvasNextPage(resp);
    }

    return users;
  }

  private static void populateUserLogins(ResponseEntity<String> resp, List<String> users) {

    JSONArray respArr = new JSONArray(resp.getBody());
    if (respArr != null) {
      IntStream.range(0, respArr.length())
          .forEach(
              i -> {
                JSONObject jsonObj = respArr.getJSONObject(i);
                if (jsonObj != null) {
                  users.add(jsonObj.getString("login_id"));
                }
              });
    }
  }

  public List<String[]> listUserDetails(String courseId) {
    List<String[]> users = new ArrayList<>();

    ResponseEntity<String> resp =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/courses/" + courseId + "/users")
                        .queryParam("enrollment_type[]", "student")
                        .queryParam("enrollment_state[]", "active")
                        .queryParam("per_page", 100)
                        .build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .toEntity(String.class)
            .block();

    populateUserDetails(resp, users);

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

      populateUserDetails(resp, users);
      nextPage = canvasNextPage(resp);
    }

    return users;
  }

  private static void populateUserDetails(ResponseEntity<String> resp, List<String[]> users) {

    JSONArray respArr = new JSONArray(resp.getBody());
    if (respArr != null) {
      IntStream.range(0, respArr.length())
          .forEach(
              i -> {
                JSONObject jsonObj = respArr.getJSONObject(i);
                if (jsonObj != null) {
                  String sortableName = jsonObj.getString("sortable_name");
                  String[] splitName = sortableName.split(",");
                  String[] userDetails;

                  if (jsonObj.has("login_id")) {
                    if (splitName.length == 2) {
                      userDetails =
                          new String[] {jsonObj.getString("login_id"), splitName[0], splitName[1]};
                    } else {
                      userDetails = new String[] {jsonObj.getString("login_id"), splitName[0], ""};
                    }
                    users.add(userDetails);
                  }
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

  public void updateFileName(String fileId, String fileName) {

    String resp =
        restClient
            .put()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/files/" + fileId)
                        .queryParam("name", fileName + ".csv")
                        .queryParam("on_duplicate", "rename")
                        .build())
            .header("Authorization", "Bearer " + oauthToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
  }

  public HashMap<String, String[]> parseFile(String url) {
    HashMap<String, String[]> resultMap = new HashMap();

    try {

      UrlResource resource = new UrlResource(url);
      if (resource.exists()) {

        CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()));
        String[] nextLine;
        int counter = 0;
        while ((nextLine = csvReader.readNext()) != null) {
          // nextLine[] is an array of values from the line
          if (counter == 0) {
            resultMap.put("headers", nextLine);
          } else {
            resultMap.put(nextLine[0], nextLine);
          }
          counter++;
        }
        if (csvReader != null) csvReader.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return resultMap;
  }

  public HashMap<String, String[]> parseFileForUser(String url, String user) {
    HashMap<String, String[]> resultMap = new HashMap();
    try {

      UrlResource resource = new UrlResource(url);
      if (resource.exists()) {

        CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()));
        String[] nextLine;
        int counter = 0;
        while ((nextLine = csvReader.readNext()) != null) {
          // nextLine[] is an array of values from the line
          if (counter == 0) {
            resultMap.put("headers", nextLine);
          } else if (nextLine[0].equalsIgnoreCase(user)) {
            resultMap.put(nextLine[0], nextLine);
            break;
          }
          counter++;
        }
        if (csvReader != null) csvReader.close();
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return resultMap;
  }

  public List<CanvasData.File> filterByUser(List<CanvasData.File> fileList, String user) {
    List<CanvasData.File> filteredList = new ArrayList<>();
    for (int i = 0; i < fileList.size(); i++) {
      CanvasData.File file = fileList.get(i);
      if (file.hidden().booleanValue() && isUserInFile(file, user)) {
        filteredList.add(file);
      }
    }
    return filteredList;
  }

  private boolean isUserInFile(CanvasData.File file, String user) {
    try {
      UrlResource resource = new UrlResource(file.url());
      if (resource.exists()) {

        CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()));
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
          if (nextLine[0].equalsIgnoreCase(user)) {
            return true;
          }
        }
        csvReader.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return false;
  }

  public ArrayList<String> validateFile(MultipartFile file, List<String> users) {

    ArrayList<String> badUsers = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
      CSVReader csvReader =
          new CSVReaderBuilder(reader)
              .withRowValidator(new RowMustHaveSameNumberOfColumnsAsFirstRowValidator())
              .build();
      String[] nextLine;

      nextLine = csvReader.readNext();
      if (!nextLine[0].equalsIgnoreCase("Login ID")) {
        badUsers.add("Column 1: Header should be Login ID");
      }
      int rowCounter = 1;
      while ((nextLine = csvReader.readNext()) != null) {
        rowCounter++;
        if (!users.contains(nextLine[0])) {
          if (nextLine[0].length() == 0) {
            badUsers.add("Row " + rowCounter + ": Login ID is blank");
          } else {
            badUsers.add("Row " + rowCounter + ": " + nextLine[0] + " is not enrolled");
          }
        }
        if (nextLine.length == 1) {
          badUsers.add("Row " + rowCounter + ": does not have content.");
        } else {
          boolean empty = true;
          for (int i = 1; i < nextLine.length; i++) {
            if (!nextLine[i].equalsIgnoreCase("")) {
              empty = false;
              break;
            }
          }
          if (empty) {
            badUsers.add("Row " + rowCounter + ": does not have content.");
          }
        }
      }
      reader.close();
    } catch (CsvValidationException csvEx) {
      log.error(csvEx.getMessage(), csvEx);
      badUsers.add(csvEx.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return badUsers;
  }

  public boolean isEmptyFile(MultipartFile file) {
    int rowCounter = 1;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
      CSVReader csvReader = new CSVReader(reader);
      String[] nextLine;
      // skip headers
      csvReader.readNext();

      while ((nextLine = csvReader.readNext()) != null) {
        rowCounter++;
      }
      reader.close();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return rowCounter == 1 ? true : false;
  }

  public boolean isCSVFile(MultipartFile file) {
    String[] fragments = file.getOriginalFilename().split("\\.");
    if (file.getContentType().equalsIgnoreCase("text/csv")
        || (file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")
            && fragments[fragments.length - 1].equalsIgnoreCase("csv"))) {
      return true;
    }
    return false;
  }
}
