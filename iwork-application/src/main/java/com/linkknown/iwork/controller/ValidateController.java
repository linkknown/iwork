package com.linkknown.iwork.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkknown.iwork.core.node.Validator;
import com.linkknown.iwork.entity.Validatelog;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.ValidatelogService;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.service.WorkStepService;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@RestController
@RequestMapping("/api/iwork")
public class ValidateController {

    // maximumPoolSize 设置为 200,拒绝策略为 AbortPolic 策略,直接抛出异常
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(100, 200, 10000,
            TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


    @Autowired
    private ValidatelogService validatelogService;
    @Autowired
    private WorkService workService;
    @Autowired
    private WorkStepService workStepService;

    @RequestMapping("/validateWork")
    public Object validateWork(@RequestParam(defaultValue = "-1") int app_id,
                               @RequestParam(defaultValue = "-1") int work_id) {
        Map<String, Object> resultMap = new HashMap<>();

        this.validateWorks(app_id, work_id);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @Data
    public class ValidatelogDetailWriter {

        private List<Validatelog.ValidatelogDetail> lst = new ArrayList<>();
        private String trackingId;

        public synchronized void write (Validatelog.ValidatelogDetail detail) {
            detail.setTrackingId(trackingId);

            lst.add(detail);
            // 一次批量写 10 条
            if (lst.size() == 10) {
                validatelogService.insertMultiValidatelogDetail(lst);
                lst.clear();
            }
        }

        public void flush () {
            validatelogService.insertMultiValidatelogDetail(lst);
        }
    }

    // 校验全部或者只校验单个 workId
    private void validateWorks(int appId, int... workIds) {
        // 日志记录 id
        String trackingId = UUID.randomUUID().toString();

        long startTime = System.currentTimeMillis();
        try {
            // 记录日志,取第一个 workId 存入
            recordValidatelogRecord(trackingId, workIds[0]);
            Work work = workService.queryWorkById(appId, workIds[0]);

            ValidatelogDetailWriter writer = new ValidatelogDetailWriter();
            writer.setTrackingId(trackingId);

            CountDownLatch countDownLatch = new CountDownLatch(workIds.length);
            for (int workId : workIds) {

                pool.execute(new Runnable() {
                    @Override
                    public void run() {

                        validateWork(appId, workId, writer);

                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();

            writer.flush();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            // 记录校验耗费时间
            this.recordCostTimeLog(trackingId, endTime - startTime);
        }
    }

    private void validateWork(int appId, int workId, ValidatelogDetailWriter writer) {
        Work work = workService.queryWorkById(appId, workId);
        List<WorkStep> workSteps = workStepService.queryWorkSteps(workId);

        CountDownLatch countDownLatch = new CountDownLatch(workSteps.size() + 1);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                // 验证流程必须以 work_start 开始,以 work_end 结束
                try {
                    checkBeginAndEnd(work, workSteps, writer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        });
        for (WorkStep workStep : workSteps) {

            WorkStep _workStep = workStep;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    validateWorkStep(work, _workStep, writer);
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkBeginAndEnd(Work work, List<WorkStep> workSteps, ValidatelogDetailWriter writer) throws InterruptedException {
        if (workSteps.size() > 0) {
            if (!StringUtils.equals(workSteps.get(0).getWorkStepType(), "work_start")) {
                Validatelog.ValidatelogDetail validatelogDetail = new Validatelog.ValidatelogDetail();
                validatelogDetail.setWorkId(work.getId());
                validatelogDetail.setWorkStepId(workSteps.get(0).getWorkStepId());
                validatelogDetail.setDetail("work must start with a work_start node!");
                validatelogDetail.setWorkName(work.getWorkName());
                validatelogDetail.setWorkStepName(workSteps.get(0).getWorkStepName());

                writer.write(validatelogDetail);
            }
            if (!StringUtils.equals(workSteps.get(workSteps.size() - 1).getWorkStepType(), "work_end")) {
                Validatelog.ValidatelogDetail validatelogDetail = new Validatelog.ValidatelogDetail();
                validatelogDetail.setWorkId(work.getId());
                validatelogDetail.setWorkStepId(workSteps.get(workSteps.size() - 1).getWorkStepId());
                validatelogDetail.setDetail("work must end with a work_end node!");
                validatelogDetail.setWorkName(work.getWorkName());
                validatelogDetail.setWorkStepName(workSteps.get(workSteps.size() - 1).getWorkStepName());

                writer.write(validatelogDetail);
            }
        }
    }

    private void validateWorkStep(Work work, WorkStep workStep, ValidatelogDetailWriter writer) {
        try {
            List<String> checkResults = getCheckResultsForStep(work, workStep);
            for (String checkResult : checkResults) {
                ObjectMapper objectMapper = new ObjectMapper();
                String paramName;
                String checkResultMsg;
                try {
                    Map map = objectMapper.readValue(checkResult, Map.class);
                    paramName = (String) map.get("paramName");
                    checkResultMsg = (String) map.get("checkResultMsg");
                } catch (JsonProcessingException e) {
                    paramName = "";
                    checkResultMsg = checkResult;
                }

                Validatelog.ValidatelogDetail validatelogDetail = new Validatelog.ValidatelogDetail();
                validatelogDetail.setWorkId(work.getId());
                validatelogDetail.setWorkStepId(workStep.getWorkStepId());
                validatelogDetail.setParamName(paramName);
                validatelogDetail.setDetail(checkResultMsg);
                validatelogDetail.setWorkName(work.getWorkName());
                validatelogDetail.setWorkStepName(workStep.getWorkStepName());

                writer.write(validatelogDetail);
            }
        } catch (Exception e) {

            Validatelog.ValidatelogDetail validatelogDetail = new Validatelog.ValidatelogDetail();
            validatelogDetail.setWorkId(work.getId());
            validatelogDetail.setWorkStepId(workStep.getWorkStepId());
            validatelogDetail.setDetail(e.getMessage() + ":" + e.getCause().getMessage());
            validatelogDetail.setWorkName(work.getWorkName());
            validatelogDetail.setWorkStepName(workStep.getWorkStepName());

            writer.write(validatelogDetail);
        }
    }

    private List<String> getCheckResultsForStep(Work work, WorkStep workStep) {
        List<String> lst = new LinkedList<>();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 校验 step 中的参数是否为空
        pool.execute(new Runnable() {
            @Override
            public void run() {
                lst.addAll(Validator.checkEmpty(workStep));
                countDownLatch.countDown();
            }
        });




        // 校验变量引用关系

        // 校验使用的函数名是否有效

        // 定制化校验
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lst;
    }

    private void recordValidatelogRecord(String trackingId, int workId) {
        Validatelog.ValidatelogRecord validatelogRecord = new Validatelog.ValidatelogRecord();
        validatelogRecord.setTrackingId(trackingId);
        validatelogRecord.setWorkId(workId);
        validatelogRecord.setCreatedBy("SYSTEM");
        validatelogRecord.setCreatedTime(new Date());
        validatelogRecord.setLastUpdatedBy("SYSTEM");
        validatelogRecord.setLastUpdatedTime(new Date());

        validatelogService.insertValidatelogRecord(validatelogRecord);
    }

    // 统计操作所花费的时间方法
    private void recordCostTimeLog(String trackingId, long costMillis) {
        String detail = String.format("validate complete! total cost %d ms!", costMillis);

        Validatelog.ValidatelogDetail validatelogDetail = Validatelog.ValidatelogDetail.getInstance(trackingId, detail);
        validatelogService.insertMultiValidatelogDetail(Arrays.asList(validatelogDetail));
    }

    // 加载校验信息,校验失败异常信息也要返回给 UI,如缓存中的 BlockStep 信息获取不到(校验异常)也要提示出来
    @RequestMapping("loadValidateResult")
    public Object loadValidateResult(@RequestParam(defaultValue = "-1") int app_id,
                               @RequestParam(defaultValue = "-1") int work_id) {
        Map<String, Object> resultMap = new HashMap<>();

        // 触发校验
        this.validateWorks(app_id, work_id);
        List<Validatelog.ValidatelogDetail> validatelogDetails = validatelogService.queryLastValidatelogDetail(work_id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("details", validatelogDetails);
        return resultMap;
    }
}
