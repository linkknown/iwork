CREATE TABLE  IF NOT EXISTS `app_id` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `app_desc` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `audit_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `task_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `task_desc` longtext COLLATE utf8_bin NOT NULL,
  `task_detail` longtext COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `cron_meta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `task_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `task_type` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `cron_str` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `enable` tinyint(1) NOT NULL DEFAULT '0',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `db_monitor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `resource_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `table_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `data_count` bigint(20) NOT NULL DEFAULT '0',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `filters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `filter_work_id` bigint(20) NOT NULL DEFAULT '0',
  `filter_work_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_name` longtext COLLATE utf8_bin NOT NULL,
  `complex_work_name` longtext COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `global_var` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `env_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `value` longtext COLLATE utf8_bin NOT NULL,
  `encrypt_flag` tinyint(1) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `desc` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  `encrypt_falg` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id` (`app_id`,`name`,`env_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `module` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `module_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `module_desc` longtext COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id` (`app_id`,`module_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `resource_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `resource_type` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `resource_link` varchar(1000) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `runlog_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracking_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `work_step_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `log_level` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `detail` longtext COLLATE utf8_bin,
  `log_order` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `tracking_id_idx` (`tracking_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `runlog_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `tracking_id` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_id` bigint(20) NOT NULL DEFAULT '0',
  `work_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `log_level` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `sql_migrate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `migrate_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `migrate_sql` longtext COLLATE utf8_bin NOT NULL,
  `migrate_hash` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `effective` tinyint(1) NOT NULL DEFAULT '0',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `sql_migrate_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracking_id` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `status` tinyint(1) NOT NULL DEFAULT '0',
  `migrate_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `tracking_detail` longtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `validatelog_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracking_id` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_id` bigint(20) NOT NULL DEFAULT '0',
  `work_step_id` bigint(20) NOT NULL DEFAULT '0',
  `work_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_step_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `param_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `success_flag` tinyint(1) NOT NULL DEFAULT '0',
  `detail` longtext COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `validatelog_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracking_id` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_id` bigint(20) NOT NULL DEFAULT '0',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL DEFAULT '0',
  `work_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_desc` longtext COLLATE utf8_bin NOT NULL,
  `work_type` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `module_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `cache_result` tinyint(1) NOT NULL DEFAULT '0',
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `work_name` (`work_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE  IF NOT EXISTS `work_step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `work_id` bigint(20) NOT NULL DEFAULT '0',
  `work_step_id` bigint(20) NOT NULL DEFAULT '0',
  `work_sub_id` bigint(20) NOT NULL DEFAULT '0',
  `work_step_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_step_desc` longtext COLLATE utf8_bin NOT NULL,
  `work_step_type` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_step_indent` int(11) NOT NULL DEFAULT '0',
  `work_step_input` longtext COLLATE utf8_bin NOT NULL,
  `work_step_output` longtext COLLATE utf8_bin NOT NULL,
  `is_defer` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `work_step_param_mapping` longtext COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  `last_updated_by` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `work_id` (`work_id`,`work_step_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


