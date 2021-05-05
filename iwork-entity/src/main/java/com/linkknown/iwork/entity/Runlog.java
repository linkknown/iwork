package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

public class Runlog {
    @Data
    public static class RunlogDetail {

        private int id;
        @JsonProperty("app_id")
        private String appId;

        @JsonProperty("tracking_id")
        private String TrackingId;
        @JsonProperty("work_step_name")
        private String WorkStepName;
        @JsonProperty("log_level")
        private String LogLevel;
        @JsonProperty("detail")
        private String Detail;
        @JsonProperty("log_order")
        private int LogOrder;
    }

    @Data
    public static class RunlogRecord {

        private int id;
        @JsonProperty("app_id")
        private String appId;

        @JsonProperty("tracking_id")
        private String TrackingId;
        @JsonProperty("work_id")
        private int WorkId;
        @JsonProperty("work_name")
        private String WorkName;
        @JsonProperty("log_level")
        private String LogLevel;

        @JsonProperty("last_updated_time")
        private Date lastUpdatedTime;
    }
}
