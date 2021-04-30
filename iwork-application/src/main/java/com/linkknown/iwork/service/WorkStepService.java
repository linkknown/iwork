package com.linkknown.iwork.service;

import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.entity.WorkStep;

import java.util.List;

public interface WorkStepService {
    List<WorkStep> queryWorkSteps(int workId);

    void insertOrUpdateWorkStep(WorkStep workStep);

    void insertStartEndWorkStepNode(int workId);

    void deleteAllWorkStep(int workId);

    WorkStep queryWorkStepInfo(int workId, int workStepId);

    void insertWorkStepAfter(int workId, int workStepId, WorkStep workStep);

    void changeReferencesWorkStepName(int workId, String oldWorkStepName, String workStepName);

    void deleteWorkStepByWorkStepId(int workId, int workStepId) throws IWorkException;

    void copyWorkStepByWorkStepId(int workId, int workStepId);

    List<WorkStep> queryAllWorkStepByWorkName(int appId, String workSubName);
}
