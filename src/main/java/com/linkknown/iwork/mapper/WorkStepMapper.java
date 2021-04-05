package com.linkknown.iwork.mapper;

import com.linkknown.iwork.entity.WorkStep;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkStepMapper {
    List<WorkStep> queryWorkSteps(int workId);

    void insertOrUpdateWorkStep(@Param("workStep") WorkStep workStep);

    void deleteAllWorkStep(int workId);

    WorkStep queryWorkStepInfo(@Param("workId") int workId, @Param("workStepId") int workStepId);

    void batchChangeWorkStepIdOrder(@Param("workId") int workId, @Param("workStepId") int workStepId, @Param("operate") String operate);

    void deleteWorkStepByWorkStepId(@Param("workId") int workId, @Param("workStepId") int workStepId);

    List<WorkStep> queryAllWorkStepByWorkName(@Param("appId") int appId, @Param("workName") String workName);
}
