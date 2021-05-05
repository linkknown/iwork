package com.linkknown.iwork.build;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.Parser;
import com.linkknown.iwork.core.WorkStepFactory;
import com.linkknown.iwork.mapper.WorkMapper;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.service.WorkStepService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
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
        Param.ParamOutputSchema runtimeParamOutputSchema = parser.getDynamicParamOutputSchema();

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
    public static void buildDynamicInput(int appId, WorkStep workStep, Consumer<WorkStep> saveOrUpdate) throws IWorkException {
        // TODO
        WorkStepFactory _parser = new WorkStepFactory();
        _parser.setWorkStep(workStep);
        _parser.setAppId(appId);
//        _parser.setWorkCache(workCache);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, _parser);

        // 获取默认数据
        Param.ParamInputSchema defaultParamInputSchema = parser.getDefaultParamInputSchema();
        // 获取动态数据
        Param.ParamInputSchema runtimeParamInputSchema = parser.getDynamicParamInputSchema();
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

    public static void buildAutoCreateSubWork(int appId, WorkStep workStep) {
        if (!StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_START)) {
            return;
        }

        WorkService workService = ApplicationContextUtil.getBean(WorkService.class);
        WorkStepService workStepService = ApplicationContextUtil.getBean(WorkStepService.class);

        WorkStepFactory _parser = new WorkStepFactory();
        _parser.setWorkStep(workStep);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, _parser);
        Param.ParamInputSchema paramInputSchema = parser.getCacheParamInputSchema();
        List<Param.ParamInputSchemaItem> paramInputSchemaItems = paramInputSchema.getParamInputSchemaItems();
        for (Param.ParamInputSchemaItem item : paramInputSchemaItems) {
            // 参数名称代表 work_sub
            if (StringUtils.equals(item.getParamName(), Constants.STRING_PREFIX + Constants.NODE_TYPE_WORK_SUB)) {
                // work_sub 名称支持纯文本和 $WORK.xxx 两种格式,统一转换成 $WORK.xxx 格式
                String workSubNameRef = StringUtils.trim(item.getParamValue());
                if (!StringUtils.startsWith(workSubNameRef, "$WORK.")) {
                    // 修改值并同步到数据库
                    item.setParamValue(String.format("$WORK.%s", workSubNameRef));
                    workStep.setWorkStepInput(paramInputSchema.renderToJson());
                } else {
                    workSubNameRef = StringUtils.removeStart(workSubNameRef, "$WORK.");
                    workSubNameRef = StringUtils.trim(workSubNameRef);
                    workSubNameRef = StringUtils.removeEnd(workSubNameRef, ";");
                }
                // 自动创建子流程
                createOrUpdateSubWork(appId, workSubNameRef);

//                workSubNameRef = IworkUtil.getSingleRelativeValueWithReg(workSubNameRef); // 去除多余的 ; 等字符
//                workSubName := strings.Replace(workSubNameRef, "$WORK.", "", -1)         // 去除前缀和多余的其它字符

                // 维护 work 的 WorkSubId 属性
                Work subWork = workService.queryWorkByName(appId, workSubNameRef);
                workStep.setWorkSubId(subWork.getId());
                break;
            }
        }
        workStepService.insertOrUpdateWorkStep(workStep);
    }

    private static void createOrUpdateSubWork(int appId, String workName) {
        WorkService workService = ApplicationContextUtil.getBean(WorkService.class);
        WorkMapper workMapper = ApplicationContextUtil.getBean(WorkMapper.class);
        WorkStepService workStepService = ApplicationContextUtil.getBean(WorkStepService.class);
        Work work = workService.queryWorkByName(appId, workName);
        if (work == null) {
            // 不存在 work 则直接创建
            work = new Work()
                    .setAppId(String.valueOf(appId))
                    .setWorkName(workName)
                    .setWorkDesc(String.format("自动创建子流程:%s", workName))
                    .setLastUpdatedTime(new Date());
            // 插入或者更新 work 信息
            workMapper.insertOrUpdateWork(work);
                // 新增 work 场景,自动添加开始和结束节点
            workStepService.insertStartEndWorkStepNode(work.getId());
        }
    }
}
