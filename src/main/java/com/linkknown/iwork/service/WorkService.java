package com.linkknown.iwork.service;


import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.Work;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WorkService {

    PageInfo<Work> queryWork(Map<String,Object> map, int page, int offset);

    Work queryWorkById(int appId, int workId);

    Work queryWorkByName(int appId, String workName);

    void editWork(Work work);

    void deleteWorkById(int workId);

    void copyWorkById(int appId, int workId);

    List<Work> queryWorksByWorkType(int appId, String workType);

    List<Work> queryAllWorks(int appId);

    Map<String, List<Work>> getRelativeWorkService(int appId, int workId);

    List<Work> queryParentWorks(int workId);
}
