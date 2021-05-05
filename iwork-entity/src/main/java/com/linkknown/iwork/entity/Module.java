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

    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
