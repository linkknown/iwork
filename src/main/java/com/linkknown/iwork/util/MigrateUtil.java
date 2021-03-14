package com.linkknown.iwork.util;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.SqlMigrate;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MigrateUtil {

    @Data
    @Accessors(chain = true)
    private static class MigrateHistory {
        private String migrateName;
        private String migrateHash;
        private boolean success;
    }

    @Data
    @Accessors(chain = true)
    private static class MigrateExecutor {
        private String trackingId;
        private boolean forceClean;
        private Resource resource;
        private List<SqlMigrate> migrates;
        // 执行历史
        private List<MigrateHistory> history;
        //
        private Consumer<SqlMigrate.SqlMigrateLog> logConsumer;

        public void run() {
            try {
                this.initial();
                this.checkHistory();
                this.migrate();
            } catch (Exception e) {
                e.printStackTrace();
                this.renderLog("", e.getMessage(), false);
            } finally {
                this.renderLog("", "__OVER__", true);
            }
        }

        private void migrate() throws IWorkException, SQLException, ClassNotFoundException {
            for (SqlMigrate migrate : this.migrates) {
                MigrateExecutor.this.migrateOne(migrate);
            }
        }

        private void migrateOne(SqlMigrate migrate) throws IWorkException, SQLException, ClassNotFoundException {
            String hash = HashUtil.getHash(migrate.getMigrateSql());
            // 已经执行过则忽略
            if (this.checkExecuted(migrate.getMigrateName(), hash)) {
                String detail = String.format("%s was migrated and skip it...", migrate.getMigrateName());
                this.renderLog(migrate.getMigrateName(), detail, true);
                return;
            }
            // 每次迁移都有可能有多个执行 sql
            String[] executeSqls = StringUtils.split(migrate.getMigrateSql(), ";");
            // 开启事务前先插入一条 false 记录
            this.insertOrUpdateMigrateVersion(migrate.getMigrateName(), hash, false);
            Connection connection = null;
            try {
                connection = DBUtil.getConnection(this.resource.getResourceUrl(), this.resource.getResourceUsername(), this.resource.getResourcePassword());
                connection.setAutoCommit(false);
                // 插入日志
                for (String sql : executeSqls) {
                    if (StringUtils.isNotBlank(StringUtils.trim(sql))) {
                        SqlUtil.execute(connection, sql);
                    }
                }
                this. insertOrUpdateMigrateVersion(migrate.getMigrateName(), hash, true);

                this.renderLog(migrate.getMigrateName(), String.format("%s was migrated success ...", migrate.getMigrateName()), true);

                connection.commit();
            } catch (Exception e) {
                if (connection != null) {
                    connection.rollback();
                }
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }

        }

        private void insertOrUpdateMigrateVersion(String migrateName, String hash, boolean successFlag) throws SQLException, ClassNotFoundException {
            if (successFlag) {
                String sql = "INSERT INTO migrate_version(migrate_name,migrate_hash,created_time, success) VALUES (?,?,NOW(), false);";
                SqlUtil.execute(this.resource.getResourceUrl(), this.resource.getResourceUsername(), this.resource.getResourcePassword(), sql, migrateName, hash);
            } else {
                String sql = "UPDATE migrate_version SET success = true where migrate_name = ?;";
                SqlUtil.execute(this.resource.getResourceUrl(), this.resource.getResourceUsername(), this.resource.getResourcePassword(), sql, migrateName);
            }

        }

        private boolean checkExecuted(String migrateName, String hash) throws IWorkException {
            MigrateHistory migrateHistory = this.history.stream().filter(_migrateHistory ->
                    StringUtils.equals(migrateName, _migrateHistory.getMigrateName()) &&
                            StringUtils.equals(hash, _migrateHistory.getMigrateHash()))
                    .findFirst().orElse(null);
            if (migrateHistory == null) {
                return false;
            } else {
                if (migrateHistory.isSuccess()) {
                    return true;
                } else {
                    throw new IWorkException(String.format("迁移文件 %s 执行失败,执行状态 success = 0,请先进行回退处理！", migrateName));
                }
            }
        }

        // 检查执行历史
        // 1、文件是否被删除
        // 2、文件是否被篡改
        // 3、文件执行顺序是否被更改
        private void checkHistory() throws IWorkException {
            String checkSql = "SELECT migrate_name, migrate_hash, success FROM migrate_version order by id asc";
            List<MigrateHistory> history = SqlUtil.executeQuery(this.resource.getResourceUrl(), this.resource.getResourceUsername(),
                    this.resource.getResourcePassword(), checkSql,
                    resultSet -> {
                        List<MigrateHistory> _history = new LinkedList<>();
                        try {
                            while (resultSet.next()) {

                                String migrate_name = resultSet.getString(1);
                                String migrate_hash = resultSet.getString(2);
                                boolean success = resultSet.getBoolean(3);
                                MigrateHistory migrateHistory = new MigrateHistory()
                                        .setMigrateName(migrate_name)
                                        .setMigrateHash(migrate_hash)
                                        .setSuccess(success);
                                _history.add(migrateHistory);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return _history;
                    });

            this.setHistory(history);

            for (int index = 0; index < this.history.size(); index++) {
                MigrateHistory migrateHistory = this.history.get(index);

                String migrate_name = migrateHistory.getMigrateName();
                String migrate_hash = migrateHistory.getMigrateHash();

                if (StringUtils.equals(migrate_name, this.migrates.get(index).getMigrateName())) {
                    if (!StringUtils.equals(migrate_hash, this.migrates.get(index).getMigrateHash())) {
                        String errorMsg = "致命错误,检测历史执行记录校验失败, 位置 %d, 历史执行记录 migrate_version 表中迁移文件名称 %s 的 hash 值是 %s, 但是 sql_migrate 表中的迁移文件 hash 值是 %s,请检查为啥内容被篡改！";
                        throw new IWorkException(String.format(errorMsg, index + 1, migrate_name, migrate_hash, this.migrates.get(index).getMigrateHash()));
                    }
                } else {
                    String errorMsg = "致命错误,检测历史执行记录校验失败,位置 %d, 历史执行记录 migrate_version 表中迁移文件名称是 %s, 但是 sql_migrate 表中的是 %s";
                    throw new IWorkException(String.format(errorMsg, index + 1, migrate_name, this.migrates.get(index).getMigrateName()));
                }
            }

        }

        private void initial() throws IWorkException, SQLException, ClassNotFoundException {
            if (this.forceClean) {
                this.executeForceClean();
            }
            this.executeVersionSql();
        }

        private void executeVersionSql() throws SQLException, ClassNotFoundException {
            String sql = "CREATE TABLE IF NOT EXISTS migrate_version (id INT(20) PRIMARY KEY AUTO_INCREMENT,\n" +
                    "MIGRATE_NAME CHAR(200), MIGRATE_HASH CHAR(200), CREATED_TIME DATETIME, SUCCESS BOOLEAN);";
            this.executeSql(sql);
        }

        private void executeForceClean() throws IWorkException, SQLException, ClassNotFoundException {
            // 去除 url 后面的问号参数 xxx_test?xxx=xxxx
            String url = StringUtils.substring(resource.getResourceUrl(), 0, StringUtils.lastIndexOf(resource.getResourceUrl(), "?"));
            if (!StringUtils.endsWith(url, "_test")) {
                throw new IWorkException("ForceClean only can used by *_test database!");
            }
            List<String> tableNames = SqlUtil.getAllTableNames(resource.getResourceUrl(), resource.getResourceUsername(), resource.getResourcePassword());
            for (String tableName : tableNames) {
                this.executeSql(String.format("DROP TABLE IF EXISTS %s;", tableName));
                this.renderLog("", String.format("table %s has clean...", tableName), true);
            }
        }

        private void executeSql(String sql) throws SQLException, ClassNotFoundException {
            SqlUtil.execute(resource.getResourceUrl(), resource.getResourceUsername(), resource.getResourcePassword(), sql);
        }

        private void renderLog(String migrateName, String detail, boolean status) {
            SqlMigrate.SqlMigrateLog migrateLog = new SqlMigrate.SqlMigrateLog();
            migrateLog.setTrackingId(trackingId);
            migrateLog.setMigrateName(migrateName);
            migrateLog.setStatus(status);
            migrateLog.setTrackingDetail(String.format("%s [%s]", detail, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            // 将日志记录到日志系统中去
            this.logConsumer.accept(migrateLog);
        }
    }

    public static void migrateToDB(int appId, String trackingId, Resource resource,
                                   List<SqlMigrate> migrates, Consumer<SqlMigrate.SqlMigrateLog> logConsumer, boolean forceClean) {
        MigrateExecutor executor = new MigrateExecutor()
                .setTrackingId(trackingId)
                .setForceClean(forceClean)
                .setResource(resource)
                .setMigrates(migrates)
                .setLogConsumer(logConsumer);
        executor.run();
    }
}
