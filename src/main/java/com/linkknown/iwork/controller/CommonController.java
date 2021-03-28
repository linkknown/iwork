package com.linkknown.iwork.controller;


import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.adapter.PageAdapter;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.util.SqlFormatterUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class CommonController {

    @Autowired
    private IworkConfig iworkConfig;

    @RequestMapping("/queryEvnNameList")
    public Object queryEvnNameList () {
        Map<String, Object> resultMap = new HashMap<>();

        String[] envNameList = iworkConfig.getEnvList().split(",");

        resultMap.put("status", "SUCCESS");
        resultMap.put("EnvNameList", envNameList);

        return resultMap;
    }

    @RequestMapping("/formatSql")
    public Object formatSql (@RequestParam("sql") String sql) {
        Map<String, Object> resultMap = new HashMap<>();

        sql = SqlFormatterUtil.format(sql);
        sql = StringUtils.removeStart(sql, "\n");
        sql = StringUtils.removeEnd(sql, "\n");
        resultMap.put("status", "SUCCESS");
        resultMap.put("sql", sql);

        return resultMap;
    }

}
