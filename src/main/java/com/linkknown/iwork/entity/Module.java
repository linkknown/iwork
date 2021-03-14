package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Module {

    private int id;

    @JsonProperty("app_id")
    private int appId;
    @JsonProperty("module_name")
    private String moduleName;
    @JsonProperty("module_desc")
    private String moduleDesc;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_time")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
