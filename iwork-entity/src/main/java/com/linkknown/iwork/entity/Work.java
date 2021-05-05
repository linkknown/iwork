package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
public class Work implements Serializable {

    private int id;
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("work_name")
    private String workName;
    @JsonProperty("work_desc")
    private String workDesc;
    @JsonProperty("work_type")
    private String workType;
    @JsonProperty("module_name")
    private String moduleName;
    @JsonProperty("cache_result")
    private boolean cacheResult;
    @JsonProperty("work_cron")
    private String workCron;

    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
