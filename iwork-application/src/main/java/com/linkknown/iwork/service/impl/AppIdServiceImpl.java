package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.mapper.AppIdMapper;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.service.AppIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AppIdServiceImpl implements AppIdService {

    @Autowired
    private AppIdMapper appIdMapper;

    @Override
    public List<AppId> getAllAppIds() {
        return appIdMapper.getAllAppIds();
    }

    @Override
    public PageInfo<AppId> queryPageAppIdList(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<AppId> appIds = appIdMapper.queryPageAppIdList(page, offset);
        PageInfo<AppId> pageInfo = new PageInfo<>(appIds);
        return pageInfo;
    }

    @Override
    public void insertOrUpdateAppId(AppId appId) {
        appIdMapper.insertOrUpdateAppId(appId);
    }

    @Override
    public void deleteAppById(int id) {
        appIdMapper.deleteAppById(id);
    }

    @Override
    public AppId getAppId(AppId appId) {
        return appIdMapper.getAppId(appId);
    }
}
