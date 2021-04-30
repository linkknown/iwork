package com.linkknown.iwork.core;

import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BlockParser {

    @Data
    public static class BlockStep {
        private Work referWork;                         // 关联的 work
        private WorkStep step;                          // 步骤
        private boolean hasChildren;                    // 是否有子步骤
        private List<BlockStep> childBlockSteps = new LinkedList<>();        // 子步骤列表
        private BlockStep parentBlockStep;              // 父级 BlockStep
        private BlockStep previousBlockStep;            // 前置 BlockStep
        private BlockStep afterBlockStep;               // 后置 BlockStep
        private List<BlockStep> siblingBlockSteps = new LinkedList<>();      // 兄弟步骤列表,包括自己
        private boolean afterJudgeInterrupt;            // 随后判断 blockStep 停止
    }


    private Work referWork;
    private List<WorkStep> steps;

    public List<BlockStep> parseToBlockSteps()  {
        List<BlockStep> blockSteps = new LinkedList<>();
        try {

            // 循环遍历将每一个 step 转换成 BlockStep
            blockSteps = this.parseToNoRelationBlockSteps();
            for (BlockStep blockStep : blockSteps) {
                // 相互之间建立关系
                this.buildRelation(blockSteps, blockStep);
            }

        } catch (IWorkException e) {
            e.printStackTrace();
        }
        return blockSteps;
    }

    public List<BlockStep> getTopLevelBlockSteps (List<BlockStep> blockSteps) {
        return blockSteps.stream().filter(blockStep -> blockStep.getParentBlockStep() == null).collect(Collectors.toList());
    }

    private void buildRelation(List<BlockStep> blockSteps, BlockStep blockStep) throws IWorkException {
        if (blockStep.getStep().getWorkStepIndent() == 0) {
            this.buildSiblingRelation(blockSteps, blockStep);
        } else {
            this.buildParentRelation(blockSteps, blockStep);
            this.buildSiblingRelation(blockSteps, blockStep);
        }
    }

    private void buildParentRelation(List<BlockStep> blockSteps, BlockStep blockStep) throws IWorkException {
        // 找最近前置缩进作为父节点
        // 1、找上一级 blockSteps
        List<BlockStep> preLevelBlockSteps = blockSteps.stream()
                .filter(blockStep1 -> blockStep1.getStep().getWorkStepIndent() + 1 == blockStep.getStep().getWorkStepIndent())
                .collect(Collectors.toList());
        if (preLevelBlockSteps == null || preLevelBlockSteps.size() == 0) {
            throw new IWorkException("无有效的父级节点");
        }

        // 找上一级 blockSteps 中比当前 blockStep 索引小的
        List<BlockStep> lowPreLevelBlockSteps = preLevelBlockSteps.stream()
                .filter(blockStep2 -> blockStep2.getStep().getWorkStepId() < blockStep.getStep().getWorkStepId())
                .collect(Collectors.toList());
        if (lowPreLevelBlockSteps == null || lowPreLevelBlockSteps.size() == 0) {
            throw new IWorkException("无有效的父级节点");
        }

        BlockStep parentBlockStep = lowPreLevelBlockSteps.get(lowPreLevelBlockSteps.size() - 1);
        // 父节点之后发现缩进大于 1 的更适合父节点
        for (int i = parentBlockStep.getStep().getWorkStepId(); i < blockStep.getStep().getWorkStepId(); i++) {
            for (WorkStep step : this.steps) {
                if (i == step.getWorkStepId() && step.getWorkStepIndent() < parentBlockStep.getStep().getWorkStepIndent()) {
                    throw new IWorkException("父节点之后发现缩进大于 1 的更适合父节点");
                }
            }
        }
        parentBlockStep.getChildBlockSteps().add(blockStep);
        parentBlockStep.setHasChildren(true);
        blockStep.setParentBlockStep(parentBlockStep);
    }

    private void buildSiblingRelation(List<BlockStep> blockSteps, BlockStep blockStep) {
        // 找最近同级别缩进作为兄弟节点
        // 1、找同级别 blockSteps
        List<BlockStep> sameLevelBlockSteps = blockSteps.stream()
                .filter(blockStep1 -> blockStep1.getStep().getWorkStepIndent() == blockStep.getStep().getWorkStepIndent())
                .collect(Collectors.toList());
        if (sameLevelBlockSteps == null || sameLevelBlockSteps.size() == 0) {
            return;
        }
        // 找同级别 blockSteps 中比当前 blockStep 索引小的
        List<BlockStep> lowSameLevelBlockSteps = sameLevelBlockSteps.stream()
                .filter(blockStep2 -> blockStep2.getStep().getWorkStepId() < blockStep.getStep().getWorkStepId())
                .collect(Collectors.toList());
        if (lowSameLevelBlockSteps == null || lowSameLevelBlockSteps.size() == 0) {
            return;
        }
        BlockStep previousSiblingBlockStep = lowSameLevelBlockSteps.get(lowSameLevelBlockSteps.size() - 1);
        previousSiblingBlockStep.setAfterBlockStep(blockStep);
        previousSiblingBlockStep.getSiblingBlockSteps().add(blockStep);

        blockStep.setPreviousBlockStep(previousSiblingBlockStep);
        blockStep.getSiblingBlockSteps().add(previousSiblingBlockStep);

        // 找前置兄弟的前置兄弟结拜
        previousSiblingBlockStep = previousSiblingBlockStep.getPreviousBlockStep();
        while (previousSiblingBlockStep != null) {
            previousSiblingBlockStep.getSiblingBlockSteps().add(blockStep);
            blockStep.getSiblingBlockSteps().add(previousSiblingBlockStep);

            previousSiblingBlockStep = previousSiblingBlockStep.getPreviousBlockStep();
        }
    }

    private List<BlockStep> parseToNoRelationBlockSteps() {
        List<BlockStep> blockSteps = new LinkedList<>();

        // 循环遍历将每一个 step 转换成 BlockStep
        for (WorkStep workStep : this.getSteps()) {
            BlockStep blockStep = new BlockStep();
            blockStep.setReferWork(this.referWork);
            blockStep.setStep(workStep);

            blockSteps.add(blockStep);
        }
        return blockSteps;
    }

    // 判断前置 step 在块范围内是否是可访问的
    public static boolean checkBlockAccessble(BlockStep currentBlockStep, int checkStepId) {
        if (StringUtils.equals(currentBlockStep.getStep().getWorkStepType(), "work_end")) { // end 节点可以访问任何前置节点输出
            return true;
        }
        for (;;) {
            // 从兄弟节点中查找
            for(BlockStep siblingBlockStep : currentBlockStep.getSiblingBlockSteps()) {
                if (siblingBlockStep.getStep().getWorkStepId() == checkStepId) {
                    return checkStepId < currentBlockStep.getStep().getWorkStepId();
                }
            }
            // 从父节点中查找
            BlockStep parentBlockStep = currentBlockStep.getParentBlockStep();
            if (parentBlockStep != null) {
                if (parentBlockStep.getStep().getWorkStepId() == checkStepId) {
                    return true;
                }
                currentBlockStep = parentBlockStep;
            } else {
                return false;
            }
        }
    }
}
