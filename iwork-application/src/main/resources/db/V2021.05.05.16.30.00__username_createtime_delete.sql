alter table app_id drop column created_by;
alter table app_id drop column created_time;
alter table app_id drop column last_updated_by;

alter table filters drop column created_by;
alter table filters drop column created_time;
alter table filters drop column last_updated_by;

alter table global_var drop column created_by;
alter table global_var drop column created_time;
alter table global_var drop column last_updated_by;

alter table module drop column created_by;
alter table module drop column created_time;
alter table module drop column last_updated_by;

alter table resource drop column created_by;
alter table resource drop column created_time;
alter table resource drop column last_updated_by;

alter table runlog_record drop column created_by;
alter table runlog_record drop column created_time;
alter table runlog_record drop column last_updated_by;

alter table sql_migrate drop column created_by;
alter table sql_migrate drop column created_time;
alter table sql_migrate drop column last_updated_by;

alter table validatelog_record drop column created_by;
alter table validatelog_record drop column created_time;
alter table validatelog_record drop column last_updated_by;

alter table work drop column created_by;
alter table work drop column created_time;
alter table work drop column last_updated_by;

alter table work_step drop column created_by;
alter table work_step drop column created_time;
alter table work_step drop column last_updated_by;