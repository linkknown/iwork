package com.linkknown.iwork.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
public class GlobalVar {

    private int id;

    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("env_name")
    private String envName;
    @JsonProperty("name")
    private String name;
    @JsonProperty("value")
    private String value;
    @JsonProperty("encrypt_flag")
    private boolean encryptFlag;
    @JsonProperty("type")
    private int type;        // 类型：0 表示不可删除
    @JsonProperty("desc")
    private String desc;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_time")
    private Date createdTime;
    @JsonProperty("last_updated_by")
    private String lastUpdatedBy;
    @JsonProperty("last_updated_time")
    private Date lastUpdatedTime;

    public static boolean isGlobalVar (String globalVarReference) {
        return StringUtils.startsWith(globalVarReference, "$Global.");
    }

    public static String getGlobalVarName (String globalVarReference) {
        globalVarReference = StringUtils.replace(globalVarReference, "$Global.", "");
        return StringUtils.replace(globalVarReference, ";", "");
    }
}
