package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.Filters;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FilterMapper {

    List<Filters> queryAllFilters(@Param("appId") int appId);

    void insertOrUpdateFilter(@Param("filter") Filters filter);
}
