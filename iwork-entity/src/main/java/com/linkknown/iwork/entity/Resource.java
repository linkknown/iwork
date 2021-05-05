package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Resource {

    private int id;
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("resource_name")
    private String resourceName;
    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("resource_link")
    private String resourceLink;

    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
