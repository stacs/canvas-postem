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

  @EmbeddedId private UserFileViewLogId id;

  @Column(nullable = false)
  private OffsetDateTime lastViewed;
}
