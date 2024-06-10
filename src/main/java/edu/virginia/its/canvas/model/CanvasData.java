package edu.virginia.its.canvas.model;

public class CanvasData {

  public record File(
      Long fileId,
      String displayName,
      String fileName,
      String url,
      Boolean locked,
      Boolean hidden,
      String updatedAt,
      String modifiedBy) {}

  public record UploadParams(
      String uploadUrl,
      String awsAccessKeyId,
      String fileName,
      String key,
      String acl,
      String policy,
      String signature,
      String successAccessRedirect,
      String contentType) {}

  public record User(Long id, String sortable_name, String login_id) {}
}
