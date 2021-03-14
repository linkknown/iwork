package com.linkknown.iwork.core.run;

import com.linkknown.iwork.core.BlockParser;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.node.FuncForRunOneStep;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

@Data
@Accessors(chain = true)
public class BlockStepOrdersRunner {

    @Data
    @Accessors(chain = true)
    public static class RunOneStepArgs {
        private String trackingId;
        private CacheLoggerWriter loggerWriter;
        private DataStore dataStore;
        private BlockParser.BlockStep blockStep;
        private Dispatcher dispatcher;
        private WorkCache workCache;
    }

    private int parentStepId;
    private WorkCache workCache;
    private String trackingId;
    private CacheLoggerWriter loggerWriter;
    private DataStore store;
    private Dispatcher dispatcher;
    private FuncForRunOneStep runOneStep;


//    RunOneStep   interfaces.RunOneStep


    public Receiver run() {
        return this.runDetail();
    }

    // runDetail 用于运行一个块,当 blockSteps 不为空时表示运行该块的一个指定节点,该节点前的都不执行,节点后的还会执行
    private Receiver runDetail(BlockParser.BlockStep... blockSteps) {
        Receiver receiver = null;
        // 存储前置步骤 afterJudgeInterrupt 属性
        boolean afterJudgeInterrupt = false;
        for (BlockParser.BlockStep blockStep : this.workCache.getBlockStepOrdersMap().get(this.parentStepId)) {
            // 当指定块中的某一个节点时,该节点之前的所有兄弟节点都不需要执行了
            if (blockSteps.length > 0 && blockStep.getStep().getWorkStepId() < blockSteps[0].getStep().getWorkStepId()) {
                continue;
            }

            RunOneStepArgs args = new RunOneStepArgs()
                    .setTrackingId(this.getTrackingId())
                    .setLoggerWriter(this.getLoggerWriter())
                    .setBlockStep(blockStep)
                    .setDataStore(this.getStore())
                    .setDispatcher(this.getDispatcher())
                    .setWorkCache(this.getWorkCache());

            if (StringUtils.equals(blockStep.getStep().getWorkStepType(), "if")) { // 遇到 if 必定可以执行
                receiver = this.runOneStep.execute(args);
                afterJudgeInterrupt = blockStep.isAfterJudgeInterrupt();
            } else if (StringUtils.equals(blockStep.getStep().getWorkStepType(), "elif") ||
                       StringUtils.equals(blockStep.getStep().getWorkStepType(), "else")) { // 遇到 elif 和 else
                if (!afterJudgeInterrupt) {
                    receiver = this.runOneStep.execute(args);
                    afterJudgeInterrupt = blockStep.isAfterJudgeInterrupt();
                }
            } else { // 非 if、elif、else 节点必定执行
                receiver = this.runOneStep.execute(args);
                afterJudgeInterrupt = false;
            }
        }
        return receiver;
    }

}
