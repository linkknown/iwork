package com.linkknown.iwork.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class IworkConfig {

    @Value("${iwork.check.white.ips}")
    private String whiteIps;

    @Value("${iwork.envname.onuse}")
    private String envOnUse;

    @Value("${iwork.envname.list}")
    private String envList;

    @Value("${iwork.internal.errorMsg}")
    private String insensitiveErrorMsg;

    @Value("${iwork.clean.runLog.second}")
    private Long cleanRunLogSecond;

}
