package com.linkknown.iwork.core.run;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.entity.Runlog;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
@Accessors(chain = true)
public class CacheLoggerWriter {
    private List<Runlog.RunlogDetail> caches = new ArrayList<>();
    Consumer<List<Runlog.RunlogDetail>> runlogDetailConsumer;
    private int logOrder;

    public static int CACHELEN = 5;

    public void write (String trackingId, String workStepName, String logLevel, String detail) {
        this.logOrder++;
        Runlog.RunlogDetail runlogDetail = new Runlog.RunlogDetail();
        runlogDetail.setTrackingId(trackingId);
        runlogDetail.setWorkStepName(workStepName);
        runlogDetail.setLogLevel(logLevel);
        runlogDetail.setDetail(detail);
        runlogDetail.setLogOrder(this.logOrder);

        this.caches.add(runlogDetail);
        if (this.caches.size() >= CACHELEN) {
            this.flush();
            this.cleanCaches();
        }
    }

    private void cleanCaches() {
        this.caches.clear();
    }

    private void flush() {
        runlogDetailConsumer.accept(this.caches);
    }

    public void close() {
        if (this.caches.size() > 0) {
            this.flush();
        }
    }

    // 统计操作所花费的时间方法
    public void recordCostTimeLog(String operateName, String trackingId, long costTime) {
        this.write(trackingId, "", Constants.LOG_LEVEL_INFO, String.format("%s total cost time :%d ms", operateName, costTime));
    }
}
