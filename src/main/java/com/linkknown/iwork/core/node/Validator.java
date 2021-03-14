package com.linkknown.iwork.core.node;

import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.Parser;
import com.linkknown.iwork.core.WorkStepFactory;
import com.linkknown.iwork.entity.WorkStep;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Validator {

    public static List<String> checkEmpty(WorkStep workStep) {
        List<String> lst = new LinkedList<>();
        try {

            if (StringUtils.isEmpty(workStep.getWorkStepName()) || StringUtils.isEmpty(workStep.getWorkStepType())) {
                lst.add("Empty workStepName or empty workStepType was found!");
            }

            WorkStepFactory _parser = new WorkStepFactory();
            _parser.setWorkStep(workStep);
            Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, _parser);
            Param.ParamInputSchema paramInputSchema = parser.getCacheParamInputSchema(workStep);
            // work_start 节点参数由调度者提供,不做非空校验
            if (!StringUtils.equals(workStep.getWorkStepType(), "work_start")) {
                paramInputSchema.getParamInputSchemaItems().forEach(new Consumer<Param.ParamInputSchemaItem>() {
                    @Override
                    public void accept(Param.ParamInputSchemaItem item) {
                        if (!StringUtils.endsWith(item.getParamName(), "?") && StringUtils.isEmpty(StringUtils.trim(item.getParamValue()))) {
                            lst.add(String.format("Empty paramValue for %s was found!", item.getParamName()));
                        }
                    }
                });
            }

        } catch (Exception e) {
            lst.add(e.getMessage() + ":" + e.getCause().getMessage());
        }
        return lst;
    }
}
