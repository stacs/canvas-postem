package edu.virginia.its.canvas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("edu.virginia.its.canvas")
@EnableJpaRepositories("edu.virginia.its.canvas.repos")
@EntityScan("edu.virginia.its.canvas.model")
public class PostemApplication {

  public static void main(String[] args) {
    SpringApplication.run(PostemApplication.class, args);
  }
}
