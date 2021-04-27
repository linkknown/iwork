package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.Module;

import java.util.List;
import java.util.Map;

public interface ModuleService {

    PageInfo<Module> queryPageModuleList(Map<String, Object> map, int page, int offset);

    void insertOrUpdateModule(Module module);

    void deleteModuleById(int id);

    List<Module> queryAllModules(int appId);
}
