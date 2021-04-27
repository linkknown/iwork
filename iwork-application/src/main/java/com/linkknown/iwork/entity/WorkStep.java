package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WorkStep {

    private int id;
    @JsonProperty("work_id")
    private int workId;
    @JsonProperty("work_step_id")
    private int workStepId;
    @JsonProperty("work_sub_id")
    private int workSubId;          // 子流程 id

    @JsonProperty("work_step_name")
    private String workStepName;
    @JsonProperty("work_step_desc")
    private String workStepDesc;
    @JsonProperty("work_step_type")
    private String workStepType;
    @JsonProperty("work_step_indent")
    private int workStepIndent;      // 调整缩进级别
    @JsonProperty("work_step_input")
    private String workStepInput;
    @JsonProperty("work_step_input_xml")
    private String workStepInputXml;
    @JsonProperty("work_step_output")
    private String workStepOutput;
    @JsonProperty("work_step_output_xml")
    private String workStepOutputXml;
    @JsonProperty("is_defer")
    private String isDefer;
    @JsonProperty("work_step_param_mapping")
    private String workStepParamMapping;
    @JsonProperty("work_step_param_mapping_xml")
    private String workStepParamMappingXml;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("lastUpdatedBy")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
