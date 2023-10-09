package edu.virginia.its.canvas.model;

import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(name = "user_file_view_log")
public class UserFileViewLog {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_id", nullable = false)
  private String loginId;

  @Column(name = "file_id", nullable = false)
  private String fileId;

  @Column(name = "last_viewed", nullable = false)
  private OffsetDateTime lastViewed;
}
