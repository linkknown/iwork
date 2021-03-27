package com.linkknown.iwork.service.impl;

import com.linkknown.iwork.dao.FilterMapper;
import com.linkknown.iwork.entity.Filters;
import com.linkknown.iwork.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterServiceImpl implements FilterService {

    @Autowired
    private FilterMapper filterMapper;

    @Override
    public List<Filters> queryAllFilters(int appId) {
        return filterMapper.queryAllFilters(appId);
    }

    @Override
    public void insertOrUpdateFilter(Filters filter) {
        filterMapper.insertOrUpdateFilter(filter);
    }
}
