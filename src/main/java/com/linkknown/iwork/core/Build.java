package com.linkknown.iwork.core;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.entity.WorkStep;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Build {

    // 构建动态输出值
    public static void buildDynamicOutput(int appId, WorkStep workStep, Consumer<WorkStep> saveOrUpdate) throws IWorkException {
        WorkStepFactory _parser = new WorkStepFactory();
        _parser.setWorkStep(workStep);
        _parser.setAppId(appId);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, _parser);

        Param.ParamOutputSchema defaultParamOutputSchema = parser.getDefaultParamOutputSchema();
        Param.ParamOutputSchema runtimeParamOutputSchema = parser.getRuntimeParamOutputSchema();

        // 合并默认数据和动态数据作为新数据
        List<Param.ParamOutputSchemaItem> newOutputSchemaItems = new LinkedList<>();
        newOutputSchemaItems.addAll(Optional.ofNullable(defaultParamOutputSchema.getParamOutputSchemaItems()).orElse(new LinkedList<>()));
        newOutputSchemaItems.addAll(Optional.ofNullable(runtimeParamOutputSchema.getParamOutputSchemaItems()).orElse(new LinkedList<>()));

        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        paramOutputSchema.setParamOutputSchemaItems(newOutputSchemaItems);

        // 构建输出参数,使用全新值
        workStep.setWorkStepOutput(paramOutputSchema.renderToJson());

        // 存储或者更新 WorkStep
        saveOrUpdate.accept(workStep);
    }

    // 构建动态输入值
    public static void buildDynamicInput(WorkStep workStep, Consumer<WorkStep> saveOrUpdate) throws IWorkException {
        // TODO
        WorkStepFactory _parser = new WorkStepFactory();
        _parser.setWorkStep(workStep);
//        _parser.setWorkCache(workCache);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, _parser);

        // 获取默认数据
        Param.ParamInputSchema defaultParamInputSchema = parser.getDefaultParamInputSchema();
        // 获取动态数据
        Param.ParamInputSchema runtimeParamInputSchema = parser.getRuntimeParamInputSchema();
        // 合并默认数据和动态数据作为新数据
        List<Param.ParamInputSchemaItem> newInputSchemaItems = new LinkedList<>();
        newInputSchemaItems.addAll(Optional.ofNullable(defaultParamInputSchema.getParamInputSchemaItems()).orElse(new LinkedList<>()));
        newInputSchemaItems.addAll(Optional.ofNullable(runtimeParamInputSchema.getParamInputSchemaItems()).orElse(new LinkedList<>()));

        // 获取历史数据
        Param.ParamInputSchema historyParamInputSchema = parser.getCacheParamInputSchema();
        for (Param.ParamInputSchemaItem newInputSchemaItem : newInputSchemaItems) {
            // 存在则不添加且沿用旧值
            // 使用 Supplier 可以解决 stream has already been operated upon or closed
            Supplier<Stream<Param.ParamInputSchemaItem>> existParamInputSchemaItemStream =
                    () -> historyParamInputSchema.getParamInputSchemaItems().stream()
                    .filter(paramInputSchemaItem -> StringUtils.equals(paramInputSchemaItem.getParamName(), newInputSchemaItem.getParamName()));
            if (existParamInputSchemaItemStream.get().count() > 0) {
                newInputSchemaItem.setParamValue(existParamInputSchemaItemStream.get().findFirst().get().getParamValue());
                newInputSchemaItem.setPureText(existParamInputSchemaItemStream.get().findFirst().get().isPureText());
            }
        }

        parser.buildParamNamingRelation(newInputSchemaItems);

        Param.ParamInputSchema paramInputSchema = new Param.ParamInputSchema();
        paramInputSchema.setParamInputSchemaItems(newInputSchemaItems);

        // workStep 修改输入 input 值
        workStep.setWorkStepInput(paramInputSchema.renderToJson());

        // 存储或者更新 WorkStep
        saveOrUpdate.accept(workStep);
    }
}
