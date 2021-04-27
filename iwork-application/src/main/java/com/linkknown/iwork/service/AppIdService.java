package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.AppId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface AppIdService {

    List<AppId> getAllAppIds ();

    PageInfo<AppId> queryPageAppIdList (Map<String, Object> map, int page, int offset);

    void insertOrUpdateAppId(AppId appId);

    void deleteAppById(int id);

    AppId getAppId(AppId appId);
}
