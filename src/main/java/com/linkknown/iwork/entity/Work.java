package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Work {

    private int id;
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("work_name")
    private String WorkName;
    @JsonProperty("work_desc")
    private String WorkDesc;
    @JsonProperty("work_type")
    private String WorkType;
    @JsonProperty("module_name")
    private String ModuleName;
    @JsonProperty("cache_result")
    private boolean CacheResult;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("lastUpdatedBy")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
