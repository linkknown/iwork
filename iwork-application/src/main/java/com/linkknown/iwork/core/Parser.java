package com.linkknown.iwork.core;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class Parser {

    public static interface IParamSchemaParser {

        Param.ParamInputSchema getDefaultParamInputSchema();
        Param.ParamInputSchema getDynamicParamInputSchema() throws IWorkException;
        Param.ParamOutputSchema getDefaultParamOutputSchema() throws IWorkException;
        Param.ParamOutputSchema getDynamicParamOutputSchema() throws IWorkException;
        void buildParamNamingRelation(List<Param.ParamInputSchemaItem> items);
    }

    public static interface IParamSchemaCacheParser extends IParamSchemaParser {
        Param.ParamInputSchema getCacheParamInputSchema(WorkStep... replaceStep);
        Param.ParamOutputSchema getCacheParamOutputSchema(WorkStep... replaceStep) throws IWorkException;
    }

    public static interface IWorkStep extends IParamSchemaParser {
        // 填充数据
        void fillParamInputSchemaDataToTmp(WorkStep workStep) throws IWorkException;
        // 填充 pureText 数据
        void fillPureTextParamInputSchemaDataToTmp(WorkStep workStep);
        Receiver getReceiver();
            // 节点执行的方法
        void execute(String trackingId) throws IWorkException;
            // 节点定制化校验函数,校验不通过会触发 panic
//        ValidateCustom(app_id int64) (checkResult []string)
    }

    @Data
    @NoArgsConstructor
    public static class ParamSchemaParser implements IParamSchemaCacheParser {

        private WorkStep workStep;
        private IParamSchemaParser paramSchemaParser;

        public ParamSchemaParser(WorkStep workStep, IParamSchemaParser paramSchemaParser) {
            this.workStep = workStep;
            this.paramSchemaParser = paramSchemaParser;
        }

        @Override
        public Param.ParamInputSchema getDefaultParamInputSchema() {
            return this.paramSchemaParser.getDefaultParamInputSchema();
        }

        @Override
        public Param.ParamInputSchema getDynamicParamInputSchema() throws IWorkException {
            return this.paramSchemaParser.getDynamicParamInputSchema();
        }

        @Override
        public Param.ParamOutputSchema getDefaultParamOutputSchema() throws IWorkException {
            return this.paramSchemaParser.getDefaultParamOutputSchema();
        }

        @Override
        public Param.ParamOutputSchema getDynamicParamOutputSchema() throws IWorkException {
            return this.paramSchemaParser.getDynamicParamOutputSchema();
        }

        @Override
        public void buildParamNamingRelation(List<Param.ParamInputSchemaItem> items) {
            this.paramSchemaParser.buildParamNamingRelation(items);
        }

        @Override
        public Param.ParamInputSchema getCacheParamInputSchema(WorkStep... replaceStep) {
            if (replaceStep != null && replaceStep.length > 0) {
                this.workStep = replaceStep[0];
            }
            // 从缓存(数据库字段)中获取
            String workStepInput = this.workStep.getWorkStepInput();
            if (StringUtils.isNotBlank(workStepInput)) {
                return Param.ParamInputSchema.parseToParamInputSchema(workStepInput);
            } else {
                if (this.paramSchemaParser != null) {
                    // 将当前 paramSchemaParser 拷贝副本
                    IParamSchemaParser parser = this.paramSchemaParser;
                    // TODO
                    return parser.getDefaultParamInputSchema();
                } else {
                    return new Param.ParamInputSchema();
                }
            }
        }

        // 获取缓存的出参 schema,即从 DB 中读取
        @Override
        public Param.ParamOutputSchema getCacheParamOutputSchema(WorkStep... replaceStep) throws IWorkException {
            if (replaceStep != null && replaceStep.length > 0) {
                this.workStep = replaceStep[0];
            }
            // 从缓存(数据库字段)中获取
            String workStepOutput = this.workStep.getWorkStepOutput();
            if (StringUtils.isNotBlank(workStepOutput)) {
                return Param.ParamOutputSchema.parseToParamOutputSchema(workStepOutput);
            } else {
                if (this.paramSchemaParser != null) {
                    // 将当前 paramSchemaParser 拷贝副本
                    IParamSchemaParser parser = this.paramSchemaParser;
                    // TODO
                    return parser.getDefaultParamOutputSchema();
                } else {
                    return new Param.ParamOutputSchema();
                }

            }
        }
    }
}
