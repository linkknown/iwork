package com.linkknown.iwork.core;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
public class ParamVauleParser {
    private String paramVaule;

    public String getNodeNameFromParamValue() {
        if (this.checkParamValueFormat()) {
            return StringUtils.substring(this.paramVaule, 1, StringUtils.indexOf(this.paramVaule, "."));
        }
        return this.getParamVaule();
    }

    public String getParamNameFromParamValue() {
        if (this.checkParamValueFormat()) {
            return StringUtils.substring(this.paramVaule, StringUtils.indexOf(this.paramVaule, ".") + 1, this.paramVaule.length());
        }
        return this.getParamVaule();
    }

    private boolean checkParamValueFormat() {
        //if strings.HasPrefix(this.ParamValue, "$") && !strings.Contains(this.ParamValue, ".") {
        //	this.ParamValue = this.ParamValue + ".__output__" // 直接引用某个节点的输出
        //}
        this.removeUnsupportChars();
        if (StringUtils.startsWith(this.getParamVaule(),"$") && StringUtils.contains(this.getParamVaule(), ".")) {
            return true;
        }
        return false;
    }

    private void removeUnsupportChars() {
        this.paramVaule = StringUtils.trim(this.getParamVaule());
        this.paramVaule = StringUtils.replace(this.getParamVaule(), "\n", "", -1);
    }
}
