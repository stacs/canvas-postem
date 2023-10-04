package edu.virginia.its.canvas.repos;

import edu.virginia.its.canvas.model.UserFileViewLog;
import edu.virginia.its.canvas.model.UserFileViewLogId;
import java.util.ArrayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileViewLogRepository
    extends JpaRepository<UserFileViewLog, UserFileViewLogId> {

  ArrayList<UserFileViewLog> findAllByIdFileId(String fileId);

  UserFileViewLog findByIdFileIdAndIdLoginId(String user, String fileId);
}
