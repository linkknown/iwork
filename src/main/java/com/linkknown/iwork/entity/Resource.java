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

    @JsonProperty("resource_url")
    private String resourceUrl;
    @JsonProperty("resource_dsn")
    private String resourceDsn;
    @JsonProperty("resource_username")
    private String resourceUsername;
    @JsonProperty("resource_password")
    private String resourcePassword;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("lastUpdatedBy")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;
}
