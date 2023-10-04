package edu.virginia.its.canvas.model;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UserFileViewLogId implements Serializable {

  private String loginId;

  private String fileId;
}
