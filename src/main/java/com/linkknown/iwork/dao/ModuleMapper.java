package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Module;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ModuleMapper {

    List<Module> queryPageModuleList(@Param("map") Map<String,Object> map, int page, int offset);

    void insertOrUpdateModule(@Param("module") Module module);

    void deleteModuleById(int id);

    List<Module> queryAllModules(int appId);
}
