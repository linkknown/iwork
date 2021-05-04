package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class AppId {

    private int id;

    @JsonProperty("app_name")
    private String appName;
    @JsonProperty("app_desc")
    private String appDesc;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_time")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;

}
