package com.linkknown.iwork.util;

import java.util.*;

public class PageUtil {
    public static int getSafePageNo(int pageNo) {
        return pageNo < 1 ? 1 : pageNo;
    }

    public static int getSafePageSize(int pageSize) {
        return (pageSize < 1 || pageSize > 100) ? 100 : pageSize;
    }


    //分页方法,根据传递过来的页数,每页数,总数,返回分页的内容 7个页数 前 1,2,3,4,5 后 的格式返回,小于5页返回具体页数
    public static Map<String, Object> paginator(int page, int prepage, int nums) {

        int firstpage = 0; //前一页地址
        int lastpage = 0;  //后一页地址
        //根据nums总数，和prepage每页数量 生成分页总数
        int totalpages = (int) Math.ceil((double) nums / (double) prepage);  //page总数

        if (page > totalpages) {
            page = totalpages;
        }
        if (page <= 0) {
            page = 1;
        }
        List<Integer> pages = new ArrayList<>();
        if (page >= totalpages-5 && totalpages > 5) {   //最后5页
            int start = totalpages - 5 + 1;
            firstpage = page - 1;
            lastpage = Math.min(totalpages, page+1);
            pages = new ArrayList<>(5);
            for (int i=0; i<pages.size(); i++) {
                pages.set(i, start + i);
            }
        } else if (page >= 3 && totalpages > 5) {
            int start = page - 3 + 1;
            pages = new ArrayList<>(5);
            firstpage = page - 3;
            for (int i=0; i<pages.size(); i++) {
                pages.set(i, start + i);
            }
            firstpage = page - 1;
            lastpage = page + 1;
        } else {
            pages = new ArrayList<>(Math.min(4, totalpages));
            for (int i=0; i< pages.size(); i++) {
                pages.set(i, i + 1);
            }
            firstpage = Math.max(1, page - 1);
            lastpage = page + 1;
        }

        Map<String, Object> paginatorMap = new HashMap<>();
        paginatorMap.put("pages", pages);
        paginatorMap.put("totalpages", totalpages);
        paginatorMap.put("firstpage", firstpage);
        paginatorMap.put("lastpage", lastpage);
        paginatorMap.put("currpage", page);
        paginatorMap.put("pagesize", prepage);
        paginatorMap.put("totalcount", nums);

        return paginatorMap;
    }

    public static List<String> getPaginatorFields() {
        return Arrays.asList("pages", "totalpages", "firstpage", "lastpage", "currpage", "pagesize", "totalcount");
    }
}
