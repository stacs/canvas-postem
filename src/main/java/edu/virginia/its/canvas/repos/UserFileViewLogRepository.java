package edu.virginia.its.canvas.repos;

import edu.virginia.its.canvas.model.UserFileViewLog;
import java.util.ArrayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileViewLogRepository extends JpaRepository<UserFileViewLog, Long> {

  ArrayList<UserFileViewLog> findAllByFileId(String fileId);

  UserFileViewLog findByFileIdAndLoginId(String fileId, String user);
}
