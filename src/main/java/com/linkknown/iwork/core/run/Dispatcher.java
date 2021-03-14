package com.linkknown.iwork.core.run;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class Dispatcher {

    private String trackingId;
    private Map<String, Object> tmpDataMap;     // 调度者发送过来的临时数据
    private Boolean existParentWork;            // 是否存在父流程
}
