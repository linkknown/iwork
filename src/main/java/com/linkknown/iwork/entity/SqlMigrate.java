package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
public class SqlMigrate {

    private int id;
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("migrate_name")
    private String migrateName;
    @JsonProperty("migrate_sql")
    private String migrateSql;

    @JsonProperty("migrate_hash")
    private String migrateHash;

    @JsonProperty("effective")
    private boolean effective;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("lastUpdatedBy")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;


    @Data
    @Accessors(chain = true)
    public static class SqlMigrateLog {
        private int id;
        @JsonProperty("tracking_id")
        private String trackingId;
        @JsonProperty("status")
        private boolean status;
        @JsonProperty("migrate_name")
        private String migrateName;
        @JsonProperty("tracking_detail")
        private String trackingDetail;
    }
}
