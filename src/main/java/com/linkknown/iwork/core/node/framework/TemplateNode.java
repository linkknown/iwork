package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@AutoRegistry
public class TemplateNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "template_text", "模板文字"));
        paramMetaList.add(new ParamMeta(Constants.COMPLEX_PREFIX + "template_dataMap", "模板变量绑定数据"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "template_text", ""));
        paramMetaList.add(new ParamMeta(Constants.COMPLEX_PREFIX + "template_dataMap", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();

        String templateValue = (String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "template_text");
        Map<String, Object> dataModel = (Map<String, Object>) this.getTmpDataMap().get(Constants.COMPLEX_PREFIX + "template_dataMap");

        String templateName = String.format("appId-workName-workStepName=[%d-%s-%s]",
                this.getAppId(), this.getWorkCache().getWork().getWorkName(), this.getWorkStep().getWorkStepName());

        try {
            String template_text = resolveFreemarkerTemplate(templateName, templateValue, dataModel);

            paramMap.put(Constants.STRING_PREFIX + "template_text", template_text);
            paramMap.put(Constants.COMPLEX_PREFIX + "template_dataMap", dataModel);
            // 将数据数据存储到数据中心
            this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
        } catch (Exception e) {
            throw new IWorkException(e.getMessage());
        }


    }

    private String resolveFreemarkerTemplate(String templateName, String templateValue, Map<String, Object> dataModel) throws IOException, TemplateException {
        // 创建一个Configuration对象，直接new一个对象。构造方法的参数就是freemarker对于的版本号
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 设置模板文件使用的字符集。一般就是utf-8
        configuration.setDefaultEncoding("utf-8");
        StringWriter stringWriter = new StringWriter();
        Template template = new Template(templateName, templateValue, configuration);
        template.process(dataModel, stringWriter);
        return stringWriter.toString();
    }

}



