package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class OperateLog {

    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("ip_addr")
    private String ipAddr;
    @JsonProperty("log")
    private String log;
    @JsonProperty("created_time")
    private Date createdTime;
}
