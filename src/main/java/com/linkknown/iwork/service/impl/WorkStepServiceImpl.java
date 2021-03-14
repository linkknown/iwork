package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.dao.WorkStepMapper;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.WorkStepService;
import com.linkknown.iwork.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WorkStepServiceImpl implements WorkStepService {

    @Autowired
    private WorkStepMapper workStepMapper;

    @Override
    public List<WorkStep> queryWorkSteps(int workId) {
        return workStepMapper.queryWorkSteps(workId);
    }

    @Override
    public void insertOrUpdateWorkStep(WorkStep workStep) {
        workStepMapper.insertOrUpdateWorkStep(workStep);
    }

    @Override
    public void insertStartEndWorkStepNode(int workId) {
        WorkStep workStep = new WorkStep();
        workStep.setWorkId(workId);
        workStep.setWorkStepId(1);
        workStep.setWorkStepName("start");
        workStep.setWorkStepDesc("start 节点");
        workStep.setWorkStepType("work_start");
        workStep.setIsDefer("false");
        workStep.setWorkStepInput("");
        workStep.setWorkStepOutput("");
        workStep.setWorkStepParamMapping("");
        workStep.setCreatedBy("SYSTEM");
        workStep.setCreatedTime(new Date());
        workStep.setLastUpdatedBy("SYSTEM");
        workStep.setLastUpdatedTime(new Date());
        workStepMapper.insertOrUpdateWorkStep(workStep);

        workStep = new WorkStep();
        workStep.setWorkId(workId);
        workStep.setWorkStepId(2);
        workStep.setWorkStepName("end");
        workStep.setWorkStepDesc("end 节点");
        workStep.setWorkStepType("work_end");
        workStep.setIsDefer("false");
        workStep.setWorkStepInput("");
        workStep.setWorkStepOutput("");
        workStep.setWorkStepParamMapping("");
        workStep.setCreatedBy("SYSTEM");
        workStep.setCreatedTime(new Date());
        workStep.setLastUpdatedBy("SYSTEM");
        workStep.setLastUpdatedTime(new Date());
        workStepMapper.insertOrUpdateWorkStep(workStep);

    }

    @Override
    public void deleteAllWorkStep(int workId) {
        workStepMapper.deleteAllWorkStep(workId);
    }

    @Override
    public WorkStep queryWorkStepInfo(int workId, int workStepId) {
        return workStepMapper.queryWorkStepInfo(workId, workStepId);
    }

    @Override
    public void insertWorkStepAfter(int workId, int workStepId, WorkStep workStep) {
        // 将 work_step_id 之后的所有节点后移一位
        workStepMapper.batchChangeWorkStepIdOrder(workId, workStepId, "add");

        workStepMapper.insertOrUpdateWorkStep(workStep);
    }

    @Override
    public void changeReferencesWorkStepName(int workId, String oldWorkStepName, String workStepName) {
        if (StringUtils.equals(oldWorkStepName, workStepName)) {
            return;
        }
        List<WorkStep> workSteps = workStepMapper.queryWorkSteps(workId);
        for (WorkStep workStep : workSteps) {
            String _workStepInput = StringUtils.replace(workStep.getWorkStepInput(), "$" + oldWorkStepName, "$" + workStepName);
            workStep.setWorkStepInput(_workStepInput);
            workStepMapper.insertOrUpdateWorkStep(workStep);
        }
    }

    @Override
    public void deleteWorkStepByWorkStepId(int workId, int workStepId) throws IWorkException {
        WorkStep workStep = workStepMapper.queryWorkStepInfo(workId, workStepId);
        if (StringUtils.equals(workStep.getWorkStepType(), "work_start") || StringUtils.equals(workStep.getWorkStepType(), "work_end")) {
            throw new IWorkException("start 节点和 end 节点不能被删除!");
        }

        workStepMapper.deleteWorkStepByWorkStepId(workId, workStepId);
        workStepMapper.batchChangeWorkStepIdOrder(workId, workStepId, "sub");
    }

    @Override
    public void copyWorkStepByWorkStepId(int workId, int workStepId) {
        // 将 work_step_id 之后的所有节点后移一位
        workStepMapper.batchChangeWorkStepIdOrder(workId, workStepId, "add");

        WorkStep workStep = workStepMapper.queryWorkStepInfo(workId, workStepId);
        workStep.setId(-1);
        workStep.setWorkStepId(++workStepId);
        workStep.setWorkStepName(workStep.getWorkStepName() + "_copy");
        workStepMapper.insertOrUpdateWorkStep(workStep);
    }

    @Override
    public List<WorkStep> queryAllWorkStepByWorkName(int appId, String workName) {
        return workStepMapper.queryAllWorkStepByWorkName(appId, workName);
    }
}
