package com.linkknown.iwork.mapper;

import com.linkknown.iwork.entity.GlobalVar;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GlobalVarMapper {

    List<GlobalVar> getAllGlobalVars();

    List<GlobalVar> queryGlobalVar(int app_id, @Param("map") Map<String,Object> map);

    void insertOrUpdateGlobalVar(@Param("globalVar") GlobalVar globalVar);

    void deleteGlobalVarById(int id);

    List<GlobalVar> queryAllGlobalVars(@Param("appId") int appId);

    GlobalVar queryGlobalVarByName(@Param("appId") int appId, @Param("name") String name, @Param("envName") String envName);
}
