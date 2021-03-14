package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.dao.GlobalVarMapper;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.service.GlobalVarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GlobalVarServiceImpl implements GlobalVarService {

    @Autowired
    private GlobalVarMapper globalVarMapper;

    @Override
    public List<GlobalVar> getAllGlobalVars() {
        return globalVarMapper.getAllGlobalVars();
    }

    @Override
    public List<GlobalVar> queryGlobalVar(int app_id, Map<String, Object> map) {
        return globalVarMapper.queryGlobalVar(app_id, map);
    }

    @Override
    public void insertOrUpdateGlobalVar(GlobalVar globalVar) {
        globalVarMapper.insertOrUpdateGlobalVar(globalVar);
    }

    @Override
    public void deleteGlobalVarById(int id) {
        globalVarMapper.deleteGlobalVarById(id);
    }

    @Override
    public List<GlobalVar> queryAllGlobalVars(int appId) {
        return globalVarMapper.queryAllGlobalVars(appId);
    }

    @Override
    public GlobalVar queryGlobalVarByName(int appId, String name, String envName) {
        return globalVarMapper.queryGlobalVarByName(appId, name, envName);
    }
}
