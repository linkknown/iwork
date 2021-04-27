package com.linkknown.iwork.service;


import com.linkknown.iwork.entity.GlobalVar;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GlobalVarService {

    List<GlobalVar> getAllGlobalVars();

    List<GlobalVar> queryGlobalVar(int app_id, @Param("map") Map<String,Object> map);

    void insertOrUpdateGlobalVar(GlobalVar globalVar);

    void deleteGlobalVarById(int id);

    List<GlobalVar> queryAllGlobalVars(int appId);

    GlobalVar queryGlobalVarByName(int appId, String name, String envName);

    String getGlobalValueForGlobalVariable(int appId, String resourceUrl);
}
