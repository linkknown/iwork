package com.linkknown.iwork.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.entity.WorkStep;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class Param {

    @Data
    @Accessors(chain = true)
    public static class ParamMapping {

        private String paramMappingName;
        private String paramMappingDefault;
        private boolean paramMappingCleanXss;
        private boolean paramMappingSafePageNo;
        private boolean paramMappingSafePageSize;

        public static List<ParamMapping> parse (String workStepParamMapping) {
            List<ParamMapping> paramMappings = new LinkedList<>();
            if (StringUtils.isNotBlank(workStepParamMapping)) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    paramMappings = objectMapper.readValue(workStepParamMapping, new TypeReference<List<ParamMapping>>() {});
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return paramMappings;
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true) // 修复 JSON 里面包含了实体没有的字段导致反序列化失败问题
    public static class ParamInputSchemaItem {

        @JsonProperty("ParamName")
        private String paramName;
        @JsonProperty("ParamDesc")
        private String paramDesc;       // 使用说明信息
        @JsonProperty("Repeatable")
        private boolean repeatable;
        @JsonProperty("ForeachRefer")
        private String foreachRefer;
        @JsonProperty("ParamChoices")
        private String[] paramChoices;
        @JsonProperty("PureText")
        private boolean pureText;
        @JsonProperty("ParamValue")
        private String paramValue;
        @JsonProperty("ParamNamings")
        private String paramNamings = "";
    }

    @Data
    public static class ParamInputSchema {
        @JsonProperty("ParamInputSchemaItems")
        private List<ParamInputSchemaItem> paramInputSchemaItems = new LinkedList<>();


        public static ParamInputSchema parseToParamInputSchema (String workStepInput) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(workStepInput, ParamInputSchema.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String renderToJson() {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = "";
            try {
                // writerWithDefaultPrettyPrinter 格式化 json
                jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return jsonStr;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ParamOutputSchemaItem {
        @JsonProperty("ParentPath")
        private String parentPath;
        @JsonProperty("ParamName")
        private String paramName;
        @JsonProperty("ParamValue")
        private String paramValue;
    }

    // 输出参数转换成 TreeNode 用于树形结构展示
    @Data
    public static class TreeNode {
        @JsonProperty("NodeName")
        private String nodeName;
        @JsonProperty("NodeLink")
        private String nodeLink;
        @JsonProperty("NodeChildrens")
        private List<TreeNode> nodeChildrens = new LinkedList<>();
    }

    @Data
    public static class ParamOutputSchema {
        @JsonProperty("ParamOutputSchemaItems")
        private List<ParamOutputSchemaItem> paramOutputSchemaItems = new LinkedList<>();

        public static ParamOutputSchema parseToParamOutputSchema (String workStepOutput) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(workStepOutput, ParamOutputSchema.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }

        public TreeNode renderToTreeNodes(String rootName) {
            // 渲染顶级节点
            TreeNode topTreeNode = new TreeNode();
            topTreeNode.setNodeName(rootName);
            topTreeNode.setNodeLink(rootName);

            for (ParamOutputSchemaItem item : paramOutputSchemaItems) {
                // 元素追加到树上面
                this.appendToTreeNodes(topTreeNode, item);
            }
            return topTreeNode;
        }

        // 元素追加到树上面
        private void appendToTreeNodes(TreeNode treeNode, ParamOutputSchemaItem item) {
            TreeNode pTreeNode = this.createAndGetParentTreeNode(treeNode, item);

            TreeNode _treeNode = new TreeNode();
            _treeNode.setNodeName(item.getParamName());
            _treeNode.setNodeLink(item.paramName);
            pTreeNode.getNodeChildrens().add(_treeNode);
        }

        private TreeNode createAndGetParentTreeNode(TreeNode treeNode, ParamOutputSchemaItem item) {
            // 父级节点是根节点
            if (StringUtils.isBlank(item.getParentPath())){
                return treeNode;
            }
            // 父级节点不是根节点
            for (TreeNode children : treeNode.getNodeChildrens()) {
                if (StringUtils.equals(children.getNodeName(), item.getParentPath())) {
                    return children;
                }
            }
            // 父级节点未曾创建过则重新创建
            TreeNode pTreeNode = new TreeNode();
            pTreeNode.setNodeName(item.getParentPath());
            pTreeNode.setNodeLink(item.getParentPath());
            treeNode.getNodeChildrens().add(pTreeNode);
            return pTreeNode;
        }

        public String renderToJson() {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = "";
            try {
                // writerWithDefaultPrettyPrinter 格式化 json
                jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return jsonStr;
        }
    }


    // 获取入参 schema
    public static ParamInputSchema getCacheParamInputSchema (WorkStep workStep) {
        WorkStepFactory factory = new WorkStepFactory();
        factory.setWorkStep(workStep);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, factory);
        return parser.getCacheParamInputSchema();
    }

    // 获取出参 schema
    public static ParamOutputSchema getCacheParamOutputSchema (WorkStep workStep) throws IWorkException {
        WorkStepFactory factory = new WorkStepFactory();
        factory.setWorkStep(workStep);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser(workStep, factory);
        return parser.getCacheParamOutputSchema();
    }
}
