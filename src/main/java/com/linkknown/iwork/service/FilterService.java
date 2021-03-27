package com.linkknown.iwork.service;

import com.linkknown.iwork.entity.Filters;

import java.util.List;

public interface FilterService {

    List<Filters> queryAllFilters(int appId);

    void insertOrUpdateFilter(Filters filter);
}
