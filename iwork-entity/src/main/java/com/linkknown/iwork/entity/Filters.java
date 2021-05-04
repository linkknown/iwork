package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class Filters {

    private int id;

    @JsonProperty("app_id")
    private int appId;
    @JsonProperty("filter_work_id")
    private int filterWorkId;
    @JsonProperty("filter_work_name")
    private String filterWorkName;
    @JsonProperty("work_name")
    private String workName;
    @JsonProperty("complex_work_name")
    private String complexWorkName;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_time")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;

}
