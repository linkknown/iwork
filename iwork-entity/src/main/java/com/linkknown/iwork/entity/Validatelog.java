package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

public class Validatelog {

    @Data
    public static class ValidatelogRecord {
        private int id;
        @JsonProperty("tracking_id")
        private String trackingId;
        @JsonProperty("work_id")
        private int workId;
        @JsonProperty("created_by")
        private String createdBy;
        @JsonProperty("lastUpdatedBy")
        private Date createdTime;
        @JsonProperty("last_updated_by")
        private String lastUpdatedBy;
        @JsonProperty("last_updated_time")
        private Date lastUpdatedTime;
    }

    @Data
    public static class ValidatelogDetail {
        private int id;
        @JsonProperty("tracking_id")
        private String trackingId;
        @JsonProperty("work_id")
        private int workId;
        @JsonProperty("work_step_id")
        private int workStepId;
        @JsonProperty("work_name")
        private String workName;
        @JsonProperty("work_step_name")
        private String workStepName;
        @JsonProperty("param_name")
        private String paramName = "";
        @JsonProperty("success_flag")
        private boolean successFlag;     // 默认就是 false
        @JsonProperty("detail")
        private String detail = "";

        @JsonProperty("created_by")
        private String createdBy = "SYSTEM";
        @JsonProperty("lastUpdatedBy")
        private Date createdTime = new Date();
        @JsonProperty("last_updated_by")
        private String lastUpdatedBy = "SYSTEM";
        @JsonProperty("last_updated_time")
        private Date lastUpdatedTime = new Date();

        public static ValidatelogDetail getInstance(String trackingId, String detail) {
            ValidatelogDetail validatelogDetail = new ValidatelogDetail();

            validatelogDetail.setTrackingId(trackingId);
            validatelogDetail.setWorkName("");
            validatelogDetail.setWorkStepName("");
            validatelogDetail.setParamName("");
            validatelogDetail.setSuccessFlag(true);
            validatelogDetail.setDetail(detail);
            validatelogDetail.setCreatedBy("SYSTEM");
            validatelogDetail.setCreatedTime(new Date());
            validatelogDetail.setLastUpdatedBy("SYSTEM");
            validatelogDetail.setLastUpdatedTime(new Date());

            return validatelogDetail;
        }
    }

}
