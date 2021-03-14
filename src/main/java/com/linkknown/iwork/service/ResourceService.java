package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.Resource;

import java.util.List;
import java.util.Map;

public interface ResourceService {

    PageInfo<Resource> queryPageResourceList(Map<String, Object> map, int page, int offset);

    void insertOrUpdateResource(Resource resource);

    Resource getResourceById(int appId, int id);

    void deleteResource(int id);

    List<Resource> queryAllResources(int appId);

    List<Resource> queryAllResources(int appId, String resourceType);

    Resource queryResourceById(int appId, int resourceId);

    Resource queryResourceByName(int appId, String resourceName);
}
