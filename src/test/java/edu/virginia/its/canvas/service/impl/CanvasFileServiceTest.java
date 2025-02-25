package edu.virginia.its.canvas.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import edu.virginia.its.canvas.PostemApplication;
import edu.virginia.its.canvas.config.SecurityConfig;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {PostemApplication.class, SecurityConfig.class})
class CanvasFileServiceTest {

  @Autowired private CanvasFileService service;

  @Test
  void testValidateFile() throws Exception {
    InputStream inputStream = new ClassPathResource("good.csv").getInputStream();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("good.csv", inputStream);
    List<String> users = List.of("tj4u");
    List<String> errors = service.validateFile(mockMultipartFile, users);
    assertTrue(errors.isEmpty());
  }

  @Test
  void testValidateFile_noLoginHeader() throws Exception {
    InputStream inputStream = new ClassPathResource("noLoginHeader.csv").getInputStream();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("noLoginHeader.csv", inputStream);
    List<String> users = List.of("tj4u");
    List<String> errors = service.validateFile(mockMultipartFile, users);
    assertTrue(errors.contains("Column 1: Header should be Login ID"));
  }

  @Test
  void testValidateFile_rowHasTooManyColumns() throws Exception {
    InputStream inputStream = new ClassPathResource("rowHasTooManyColumns.csv").getInputStream();
    MockMultipartFile mockMultipartFile =
        new MockMultipartFile("rowHasTooManyColumns.csv", inputStream);
    List<String> users = List.of("tj4u");
    List<String> errors = service.validateFile(mockMultipartFile, users);
    assertTrue(errors.contains("Row was expected to have 3 elements but had 4 instead\n"));
  }
}
