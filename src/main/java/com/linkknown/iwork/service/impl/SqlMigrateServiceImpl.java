package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.dao.SqlMigrateMapper;
import com.linkknown.iwork.entity.SqlMigrate;
import com.linkknown.iwork.service.SqlMigrateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlMigrateServiceImpl implements SqlMigrateService {

    @Autowired
    private SqlMigrateMapper sqlMigrateMapper;

    @Override
    public PageInfo<SqlMigrate> queryPageSqlMigrates(int appId, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<SqlMigrate> migrates = sqlMigrateMapper.queryPageSqlMigrates(appId, page, offset);
        PageInfo<SqlMigrate> pageInfo = new PageInfo<>(migrates);
        return pageInfo;
    }

    @Override
    public SqlMigrate queryMigrateById(int appId, int id) {
        return sqlMigrateMapper.queryMigrateById(appId, id);
    }

    @Override
    public void insertOrUpdateSqlMigrate(SqlMigrate migrate) {
        sqlMigrateMapper.insertOrUpdateSqlMigrate(migrate);
    }

    @Override
    public List<SqlMigrate> queryAllMigrates(int appId) {
        return sqlMigrateMapper.queryAllMigrates(appId);
    }

    @Override
    public List<SqlMigrate.SqlMigrateLog> queryMigrateLogsByTrackingId(String trackingId) {
        return sqlMigrateMapper.queryMigrateLogsByTrackingId(trackingId);
    }

    @Override
    public void insertSqlMigrateLogs(List<SqlMigrate.SqlMigrateLog> logs) {
        if (logs != null && logs.size() > 0) {
            sqlMigrateMapper.insertSqlMigrateLogs(logs);
        }
    }
}
