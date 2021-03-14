package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.AppId;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppIdMapper {

    List<AppId> getAllAppIds ();

    List<AppId> queryPageAppIdList(int page, int offset);

    void insertOrUpdateAppId(@Param("appId") AppId appId);

    void deleteAppById(int id);

    AppId getAppId(@Param("appId") AppId appId);
}
