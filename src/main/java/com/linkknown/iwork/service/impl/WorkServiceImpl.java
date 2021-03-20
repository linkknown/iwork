package com.linkknown.iwork.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.dao.WorkMapper;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.service.WorkStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkServiceImpl implements WorkService {
    @Autowired
    private WorkMapper workMapper;
    @Autowired
    private WorkStepService workStepService;


    @Override
    public PageInfo<Work> queryWork(Map<String, Object> map, int page, int offset) {
        PageHelper.startPage(page, offset);
        List<Work> works = workMapper.queryWork(map, page, offset);
        PageInfo<Work> pageInfo = new PageInfo<>(works);
        return pageInfo;
    }

    @Override
    public Work queryWorkById(int appId, int workId) {
        return workMapper.queryWorkById(appId, workId);
    }

    @Override
    public Work queryWorkByName(int appId, String workName) {
        return workMapper.queryWorkByName(appId, workName);
    }

    @Override
    public void editWork(Work work) {
        Work oldWork = workMapper.queryWorkById(Integer.parseInt(work.getAppId()), work.getId());
        // // 插入或者更新 work 信息
        workMapper.insertOrUpdateWork(work);
        // 重新查出来
        work = workMapper.queryWorkByName(Integer.parseInt(work.getAppId()), work.getWorkName());
        if (oldWork == null) {
            // 新增 work 场景,自动添加开始和结束节点
            workStepService.insertStartEndWorkStepNode(work.getId());
        } else {
            // 修改 work 场景
            // TODO
//            if err := ChangeReferencesWorkName(oldWorkId, oldWorkName, work.WorkName, o); err != nil {
//                return err
//            }
        }
    }

    @Override
    public void deleteWorkById(int workId) {
        workStepService.deleteAllWorkStep(workId);
        workMapper.deleteWorkById(workId);
    }

    @Override
    public void copyWorkById(int appId, int workId) {
        Work work = workMapper.queryWorkById(appId, workId);
        // 拷贝流程
        work.setId(-1);
        work.setWorkName(work.getWorkName() + "_copy");
        workMapper.insertOrUpdateWork(work);

        // 拷贝步骤
        List<WorkStep> workSteps = workStepService.queryWorkSteps(workId);
        for (WorkStep workStep : workSteps) {
            workStep.setId(-1);
            workStep.setWorkId(work.getId());

            workStepService.insertOrUpdateWorkStep(workStep);
        }
    }

    @Override
    public List<Work> queryWorksByWorkType(int appId, String workType) {
        return workMapper.queryWorksByWorkType(appId, workType);
    }

    @Override
    public List<Work> queryAllWorks(int appId) {
        return workMapper.queryAllWorks(appId);
    }

    @Override
    public Map<String, List<Work>> getRelativeWorkService(int appId, int workId) {
        Map<String, List<Work>> result = new HashMap<>();
        List<Work> parentWorks = new ArrayList<>();
        List<Work> subworks = new ArrayList<>();

        parentWorks = workMapper.queryParentWorks(workId);
        List<WorkStep> workSteps = workStepService.queryWorkSteps(workId);
        for (WorkStep workStep : workSteps) {
            if (workStep.getWorkSubId() > 0) {
                Work work = workMapper.queryWorkById(appId, workStep.getWorkSubId());
                subworks.add(work);
            }
        }
        result.put("parentWorks", parentWorks);
        result.put("subworks", subworks);
        return result;
    }
}
