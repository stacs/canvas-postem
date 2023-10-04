CREATE TABLE user_file_view_log (
 `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `file_id` varchar(255) NOT NULL,
 `last_viewed` timestamp,
 `login_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;