package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.mapper.ModuleMapper;
import com.linkknown.iwork.entity.Module;
import com.linkknown.iwork.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ModuleServiceImpl implements ModuleService {
    @Autowired
    private ModuleMapper moduleMapper;

    @Override
    public PageInfo<Module> queryPageModuleList(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<Module> modules = moduleMapper.queryPageModuleList(map, page, offset);
        PageInfo<Module> pageInfo = new PageInfo<>(modules);
        return pageInfo;
    }

    @Override
    public void insertOrUpdateModule(Module module) {
        moduleMapper.insertOrUpdateModule(module);
    }

    @Override
    public void deleteModuleById(int id) {
        moduleMapper.deleteModuleById(id);
    }

    @Override
    public List<Module> queryAllModules(int appId) {
        return moduleMapper.queryAllModules(appId);
    }
}
