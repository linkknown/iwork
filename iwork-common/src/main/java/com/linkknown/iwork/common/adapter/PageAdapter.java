package com.linkknown.iwork.common.adapter;

import com.github.pagehelper.PageInfo;
import lombok.Data;

public class PageAdapter {

    public static String[] getPaginatorFields() {
        return new String[]{"pages", "totalpages", "firstpage", "lastpage", "currpage", "pagesize", "totalcount"};
    }

    @Data
    public static class Paginator {
        private int totalpages;
        private int firstpage;
        private int lastpage;
        private int currpage;
        private int pagesize;
        private int totalcount;
    }

    public static <T> Paginator getPaginator(PageInfo<T> pageInfo) {
        Paginator paginator = new Paginator();
        paginator.setTotalpages(pageInfo.getPages());
        paginator.setFirstpage(pageInfo.getFirstPage());
        paginator.setLastpage(pageInfo.getLastPage());
        paginator.setCurrpage(pageInfo.getPageNum());
        paginator.setPagesize(pageInfo.getPageSize());
        paginator.setTotalcount((int) pageInfo.getTotal());
        return paginator;
    }

}
