package edu.virginia.its.canvas.controller;

import edu.virginia.its.canvas.lti.util.CanvasAuthenticationToken;
import edu.virginia.its.canvas.model.*;
import edu.virginia.its.canvas.repos.UserFileViewLogRepository;
import edu.virginia.its.canvas.service.impl.CanvasFileService;
import edu.virginia.its.canvas.service.impl.CanvasUserService;
import edu.virginia.its.canvas.util.Constants;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PostemController {

  @Value("${ltitool.canvas.apiUrl}")
  private String APIHOST;

  @Value("${ltitool.canvas.apiToken}")
  private String APITOKEN;

  @Autowired private CanvasFileService canvasFileService;
  @Autowired private CanvasUserService canvasUserService;
  @Autowired UserFileViewLogRepository userFileViewLogRepository;

  private final MessageSource messageSource;

  @GetMapping("/launch")
  public String launch(Model model) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String roles = token.getCustomValue(Constants.CANVAS_ROLES);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    log.info(
        "Authenticated LTI request by Posted Feedback for user: "
            + userId
            + " in course: "
            + courseId
            + " with roles: "
            + roles);

    if (roles.contains("TeacherEnrollment")
        || roles.contains("TaEnrollment")
        || roles.contains("Account Admin")) {

      model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
      model.addAttribute("user", user);
      model.addAttribute("courseId", courseId);
      model.addAttribute("userId", userId);

      return "instructor/index";
    } else if (roles.contains("StudentEnrollment")) {

      List<CanvasData.File> fileList = canvasFileService.listFiles(courseId, timeZone);
      List<CanvasData.File> filteredList = canvasFileService.filterByUser(fileList, user);
      model.addAttribute("courseFiles", filteredList);
      model.addAttribute("user", user);
      model.addAttribute("courseId", courseId);
      model.addAttribute("userId", userId);

      return "student/index";
    }

    return "unauthorized";
  }

  @GetMapping("/instructorHome")
  public String index(Model model) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("courseId", courseId);
    return "instructor/index";
  }

  @GetMapping("/viewFile")
  public String viewFile(Model model, @RequestParam String fileId) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    ArrayList<UserFileViewLog> userActivity = userFileViewLogRepository.findAllByFileId(fileId);
    CanvasData.File file = canvasFileService.fetchFile(Long.parseLong(fileId), timeZone);
    HashMap<String, String[]> parsedFileMap = canvasFileService.parseFile(file.url());
    String[] headerArray = parsedFileMap.get("headers");
    parsedFileMap.remove("headers");

    HashMap<String, UserFileViewLog> activityMap = new HashMap<>();
    userActivity.stream()
        .forEach(
            activity -> {
              activityMap.put(activity.getLoginId(), activity);
            });

    model.addAttribute("headers", headerArray);
    model.addAttribute("contents", parsedFileMap.values());
    model.addAttribute("displayName", file.displayName());
    model.addAttribute("released", file.hidden());
    model.addAttribute("userActivity", activityMap);
    model.addAttribute("courseId", courseId);
    model.addAttribute("userId", userId);

    return "instructor/viewFile";
  }

  @GetMapping("/studentView")
  public String studentView(Model model, @RequestParam String fileId) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    CanvasData.File file = canvasFileService.fetchFile(Long.parseLong(fileId), timeZone);
    HashMap<String, String[]> parsedFileMap = canvasFileService.parseFile(file.url());
    String[] headerArray = parsedFileMap.get("headers");
    parsedFileMap.remove("headers");

    HashMap<String, String> courseUsersMap = canvasUserService.getUsersOfCourse(courseId);
    HashMap<String, String> userMap = new HashMap<>();

    parsedFileMap.forEach(
        (key, val) -> {
          if (courseUsersMap.containsKey(val[0])) {
            userMap.put(key, courseUsersMap.get(val[0]) + " (" + val[0] + ")");
          } else {
            userMap.put(key, " (" + val[0] + ")");
          }
        });

    HashMap<String, String> sortedMap =
        userMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getValue().toLowerCase()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

    model.addAttribute("logins", sortedMap);
    model.addAttribute("displayName", file.displayName());
    model.addAttribute("fileId", fileId);
    model.addAttribute("user", user);
    model.addAttribute("courseId", courseId);
    model.addAttribute("userId", userId);

    return "instructor/studentView";
  }

  @GetMapping("/studentFileInfo")
  public ResponseEntity<?> studentFileInfo(@RequestParam String fileId, @RequestParam String user) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              messageSource.getMessage(
                  "dashboard.unauthorized", null, LocaleContextHolder.getLocale()));
    }

    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    CanvasData.File file = canvasFileService.fetchFile(Long.parseLong(fileId), timeZone);
    HashMap<String, String[]> response = canvasFileService.parseFileForUser(file.url(), user);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/editFile")
  public String editFile(
      Model model,
      @RequestParam String editType,
      @RequestParam String fileId,
      @RequestParam String displayName) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    model.addAttribute("editType", editType);
    model.addAttribute("user", user);
    model.addAttribute("fileId", fileId);
    model.addAttribute("displayName", displayName);
    model.addAttribute("courseId", courseId);
    model.addAttribute("userId", userId);

    return "instructor/editFile";
  }

  @PostMapping("/upload")
  public String upload(
      Model model,
      @RequestParam MultipartFile myFile,
      @RequestParam String fileTitle,
      @RequestParam(required = false, defaultValue = "false") boolean releaseCheckbox) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    ArrayList<String> users = canvasFileService.listUserLogins(courseId);

    model.addAttribute("user", user);
    model.addAttribute("courseId", courseId);
    model.addAttribute("userId", userId);

    if (!canvasFileService.isCSVFile(myFile)) {

      model.addAttribute("status", "error");
      model.addAttribute(
          "description",
          messageSource.getMessage("error.invalidformat", null, LocaleContextHolder.getLocale()));
    } else if (canvasFileService.isEmptyFile(myFile)) {

      model.addAttribute("status", "error");
      model.addAttribute(
          "description",
          messageSource.getMessage("error.emptyfile", null, LocaleContextHolder.getLocale()));
    } else {
      ArrayList<String> badUsers = canvasFileService.validateFile(myFile, users);
      if (badUsers.size() > 0) {
        model.addAttribute("status", "error");
        model.addAttribute("badUsers", badUsers);
        model.addAttribute(
            "description",
            messageSource.getMessage("error.invalidusers", null, LocaleContextHolder.getLocale()));

      } else {
        fileTitle = fileTitle + ".csv";
        Boolean releaseFeedback = false;
        if (releaseCheckbox) {
          releaseFeedback = true;
        }
        long fileId =
            canvasFileService.upload(myFile, true, releaseFeedback, courseId, fileTitle, userId);
        if (fileId != -1) {
          model.addAttribute("status", "success");
          model.addAttribute("description", fileTitle + " successfully uploaded");

        } else {
          model.addAttribute("status", "error");
          model.addAttribute("description", fileTitle + " upload failed");
        }
      }
    }

    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));

    return "instructor/index";
  }

  @PostMapping("/uploadNewVersion")
  public String uploadNewVersion(
      Model model,
      @RequestParam MultipartFile myFileNewVersion,
      @RequestParam String fileTitle,
      @RequestParam String selectedFileId,
      @RequestParam(required = false, defaultValue = "false") boolean releaseCheckbox) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String userId = token.getCustomValue(Constants.CANVAS_USER_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    ArrayList<String> users = canvasFileService.listUserLogins(courseId);

    model.addAttribute("user", user);
    model.addAttribute("courseId", courseId);
    model.addAttribute("userId", userId);
    model.addAttribute("fileId", selectedFileId);
    model.addAttribute("displayName", fileTitle);

    if (!canvasFileService.isCSVFile(myFileNewVersion)) {

      model.addAttribute("editType", "add");
      model.addAttribute("status", "error");
      model.addAttribute(
          "description",
          messageSource.getMessage("error.invalidformat", null, LocaleContextHolder.getLocale()));
      return "instructor/editFile";

    } else if (canvasFileService.isEmptyFile(myFileNewVersion)) {

      model.addAttribute("editType", "add");
      model.addAttribute("status", "error");
      model.addAttribute(
          "description",
          messageSource.getMessage("error.emptyfile", null, LocaleContextHolder.getLocale()));
      return "instructor/editFile";
    } else {
      ArrayList<String> badUsers = canvasFileService.validateFile(myFileNewVersion, users);
      if (badUsers.size() > 0) {
        model.addAttribute("status", "error");
        model.addAttribute("badUsers", badUsers);
        model.addAttribute("editType", "add");
        model.addAttribute(
            "description",
            messageSource.getMessage("error.invalidusers", null, LocaleContextHolder.getLocale()));
        return "instructor/editFile";

      } else {
        fileTitle = fileTitle + ".csv";
        Boolean releaseFeedback = false;
        if (releaseCheckbox) {
          releaseFeedback = true;
        }
        long fileId =
            canvasFileService.upload(
                myFileNewVersion, true, releaseFeedback, courseId, fileTitle, userId);
        if (fileId != -1) {

          model.addAttribute("status", "success");
          model.addAttribute("description", fileTitle + " successfully uploaded");

        } else {

          model.addAttribute("status", "error");
          model.addAttribute("description", fileTitle + " upload failed");
        }
      }
    }

    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));

    return "instructor/index";
  }

  @GetMapping("/delete")
  public String delete(Model model, @RequestParam String fileId) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    String fileName = canvasFileService.deleteFile(fileId);

    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("status", "success");
    model.addAttribute("description", fileName + " successfully deleted.");

    return "instructor/index";
  }

  @GetMapping("/release")
  public String release(
      Model model, @RequestParam String fileId, @RequestParam String displayName) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    canvasFileService.hideFile(fileId, true);
    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("status", "success");
    model.addAttribute("description", displayName + " successfully released to students.");

    return "instructor/index";
  }

  @GetMapping("/unrelease")
  public String unrelease(
      Model model, @RequestParam String fileId, @RequestParam String displayName) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    canvasFileService.hideFile(fileId, false);
    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("status", "success");
    model.addAttribute("description", displayName + " successfully unreleased.");

    return "instructor/index";
  }

  @GetMapping("/downloadFile")
  public ResponseEntity<?> downloadFile() {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              messageSource.getMessage(
                  "dashboard.unauthorized", null, LocaleContextHolder.getLocale()));
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);

    String[] headers = {"Login ID", "Last Name", "First Name"};
    List<String[]> userList = canvasFileService.listUserDetails(courseId);

    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template.csv");

    StringBuilder builder = new StringBuilder();
    builder.append(Arrays.stream(headers).collect(Collectors.joining(",")) + "\n");

    for (String[] users : userList) {
      String data = Arrays.stream(users).collect(Collectors.joining(",")) + "\n";
      builder.append(data);
    }

    return ResponseEntity.ok()
        .headers(header)
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(builder.toString());
  }

  @GetMapping("/downloadCSV")
  public ResponseEntity<?> downloadCSV(Model model, @RequestParam String fileId) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              messageSource.getMessage(
                  "dashboard.unauthorized", null, LocaleContextHolder.getLocale()));
    }

    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    CanvasData.File file = canvasFileService.fetchFile(Long.parseLong(fileId), timeZone);

    ArrayList<UserFileViewLog> userActivity = userFileViewLogRepository.findAllByFileId(fileId);
    Map<String, String[]> parsedFileMap = canvasFileService.parseFile(file.url());

    HttpHeaders header = new HttpHeaders();
    header.add(
        HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.displayName() + ".csv");

    StringBuilder builder = new StringBuilder();

    parsedFileMap.forEach(
        (key, value) -> {
          if (key.equalsIgnoreCase("headers")) {
            String columnHeaders =
                Arrays.stream(value).collect(Collectors.joining(",")) + ",Last Viewed\n";
            builder.append(columnHeaders);
          }
        });

    parsedFileMap.forEach(
        (key, value) -> {
          if (!key.equalsIgnoreCase("headers")) {
            String columnData = Arrays.stream(value).collect(Collectors.joining(","));
            boolean activityFound = false;
            for (UserFileViewLog activity : userActivity) {
              if (activity.getLoginId().equalsIgnoreCase(key)) {
                activityFound = true;
                columnData += "," + activity.getLastViewed().toString() + "\n";
              }
            }
            if (!activityFound) {
              columnData += ",Never\n";
            }
            builder.append(columnData);
          }
        });

    return ResponseEntity.ok()
        .headers(header)
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(builder.toString());
  }

  @PostMapping("/renameFile")
  public String renameFile(
      Model model, @RequestParam String fileId, @RequestParam String fileName) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    canvasFileService.updateFileName(fileId, fileName);
    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("status", "success");
    model.addAttribute("description", "Feedback file successfully renamed to " + fileName + ".");

    return "instructor/index";
  }

  @GetMapping("/handleSizeLimitExceededException")
  public String handleSizeLimitExceededException(Model model) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);

    model.addAttribute("courseFiles", canvasFileService.listFiles(courseId, timeZone));
    model.addAttribute("status", "error");
    model.addAttribute(
        "description",
        messageSource.getMessage("error.filesize.exceeded", null, LocaleContextHolder.getLocale()));
    return "instructor/index";
  }

  @GetMapping("/studentHome")
  public String studentHome(Model model) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    List<CanvasData.File> fileList = canvasFileService.listFiles(courseId, timeZone);
    List<CanvasData.File> filteredList = canvasFileService.filterByUser(fileList, user);
    model.addAttribute("courseFiles", filteredList);
    return "student/index";
  }

  @GetMapping("/viewFileByStudent")
  public String viewFileByStudent(Model model, @RequestParam String fileId) {

    CanvasAuthenticationToken token;

    try {
      token = CanvasAuthenticationToken.getToken();
    } catch (Exception e) {
      return "unauthorized";
    }

    String courseId = token.getCustomValue(Constants.CANVAS_COURSE_ID);
    String timeZone = token.getCustomValue(Constants.CANVAS_TIMEZONE);
    String user = token.getCustomValue(Constants.CANVAS_USER);

    CanvasData.File file = canvasFileService.fetchFile(Long.parseLong(fileId), timeZone);

    // log user viewing file
    UserFileViewLog lastViewed = userFileViewLogRepository.findByFileIdAndLoginId(fileId, user);
    if (lastViewed != null) {
      lastViewed.setLastViewed(OffsetDateTime.now());
    } else {
      lastViewed = new UserFileViewLog();
      lastViewed.setLastViewed(OffsetDateTime.now());
      lastViewed.setFileId(fileId);
      lastViewed.setLoginId(user);
    }
    userFileViewLogRepository.saveAndFlush(lastViewed);

    HashMap<String, String[]> parsedFileMap = canvasFileService.parseFileForUser(file.url(), user);
    String[] headerArray = parsedFileMap.get("headers");
    parsedFileMap.remove("headers");

    model.addAttribute("headers", headerArray);
    model.addAttribute("contents", parsedFileMap.values());
    model.addAttribute("courseId", courseId);
    model.addAttribute("user", user);

    return "student/viewFile";
  }
}
