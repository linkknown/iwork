package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ResourceMapper {
    List<Resource> queryPageResourceList(@Param("map") Map<String,Object> map, int page, int offset);

    void insertOrUpdateResource(@Param("resource") Resource resource);

    Resource queryResourceById(@Param("appId") int appId, @Param("id") int id);

    void deleteResource(int id);

    List<Resource> queryAllResources(@Param("appId") int appId, @Param("resourceType") String resourceType);

    Resource queryResourceByName(@Param("appId") int appId, @Param("resourceName") String resourceName);
}
