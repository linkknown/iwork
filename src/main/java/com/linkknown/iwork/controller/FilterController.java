package com.linkknown.iwork.controller;

import com.linkknown.iwork.entity.Filters;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.service.FilterService;
import com.linkknown.iwork.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class FilterController {

    @Autowired
    private WorkService workService;
    @Autowired
    private FilterService filterService;

    @RequestMapping("/saveFilters")
    public Object saveFilters(@RequestParam(defaultValue = "-1") int app_id,
                              @RequestParam(defaultValue = "-1") int filter_id,
                              @RequestParam(defaultValue = "") String workNames,
                              @RequestParam(defaultValue = "") String complexWorkName) {
        Map<String, Object> resultMap = new HashMap<>();

        Work filterWork = workService.queryWorkById(app_id, filter_id);

        Filters filter = new Filters();
        filter.setAppId(app_id);
        filter.setFilterWorkId(filterWork.getId());
        filter.setFilterWorkName(filterWork.getWorkName());
        filter.setWorkName(workNames);
        filter.setComplexWorkName(complexWorkName);
        filter.setCreatedBy("SYSTEM");
        filter.setCreatedTime(new Date());
        filter.setLastUpdatedBy("SYSTEM");
        filter.setLastUpdatedTime(new Date());

        filterService.insertOrUpdateFilter(filter);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }


}

