CREATE TABLE `operate_log` (
    user_name varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
    ip_addr varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
    log varchar(4000) COLLATE utf8_bin NOT NULL DEFAULT '',
    created_time datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;