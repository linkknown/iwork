package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.dao.AppIdMapper;
import com.linkknown.iwork.dao.ResourceMapper;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.service.ResourceService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public PageInfo<Resource> queryPageResourceList(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<Resource> resources = resourceMapper.queryPageResourceList(map, page, offset);
        PageInfo<Resource> pageInfo = new PageInfo<>(resources);
        return pageInfo;
    }

    @Override
    public void insertOrUpdateResource(Resource resource) {
        resourceMapper.insertOrUpdateResource(resource);
    }

    @Override
    public Resource getResourceById(int appId, int id) {
        return resourceMapper.queryResourceById(appId, id);
    }

    @Override
    public void deleteResource(int id) {
        resourceMapper.deleteResource(id);
    }

    @Override
    public List<Resource> queryAllResources(int appId) {
        return resourceMapper.queryAllResources(appId, null);
    }

    @Override
    public List<Resource> queryAllResources(int appId, String resourceType) {
        return resourceMapper.queryAllResources(appId, resourceType);
    }

    @Override
    public Resource queryResourceById(int appId, int resourceId) {
        return resourceMapper.queryResourceById(appId, resourceId);
    }

    @Override
    public Resource queryResourceByName(int appId, String resourceName) {
        return resourceMapper.queryResourceByName(appId, resourceName);
    }

}
