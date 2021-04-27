package com.linkknown.iwork.filter;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.common.web.MutableHttpServletRequest;
import com.linkknown.iwork.core.Memory;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.core.run.Runner;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.entity.Filters;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.RunLogService;
import com.linkknown.iwork.util.DatatypeUtil;
import com.linkknown.iwork.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Order(3)
@WebFilter(urlPatterns = "/api/iwork/httpservice/*")
public class IworkFilter implements Filter {

    @Autowired
    private RunLogService runLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String uri = httpServletRequest.getRequestURI();
        uri = StringUtils.removeStart(uri, "/api/iwork/httpservice/");
        String[] pathVariables = StringUtils.splitByWholeSeparator(uri, "/");

        Assert.isTrue(pathVariables != null && pathVariables.length == 2, "请求路由不合法！");

        String appName = pathVariables[0];
        String workName = pathVariables[1];

        // A-Z: 65-90
        if (workName.charAt(0) < 65 || workName.charAt(0) > 90) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("status", "ERROR");
            errorMap.put("errorMsg", "The api interface is not accessible!");

            httpServletResponse.setStatus(550);
            httpServletResponse.getWriter().write(JsonUtil.writeToString(errorMap));
            return;
        }

        AppId appId = Memory.getAppIdFromMemory(-1, appName);
        List<Filters> filters = Memory.getAllFilterWithOrder(appId.getId());

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(httpServletRequest);

        boolean passFlag = true;
        for (Filters filter : filters) {
           int status = runFilter(appId, workName, filter, mutableRequest, httpServletResponse);
           if (status != 200) {
               passFlag = false;
               httpServletResponse.setStatus(status);
               break;
           }
        }
        if (passFlag) {
            filterChain.doFilter(mutableRequest, response);
        }
    }

    private int runFilter(AppId appId, String workName, Filters filter, MutableHttpServletRequest mutableRequest, HttpServletResponse response) {
        int status = 200;
        if (canIntercept(workName, filter, mutableRequest)) {
            WorkCache filterWorkCache = Memory.getWorkCacheByNameFromMemory(appId.getId(), filter.getFilterWorkName());

            // 获取请求参数
            Map<String, Object> mapData = this.parseParam(filterWorkCache, mutableRequest, filterWorkCache.getSteps().get(0));
            // 传递 request 对象
            mapData.put(Constants.HTTP_REQUEST_OBJECT, mutableRequest);

            Receiver receiver = new Runner()
                    .setRunlogRecordConsumer(runlogRecord -> runLogService.insertRunlogRecord(runlogRecord))
                    .setRunlogDetailConsumer(runlogDetails -> runLogService.insertMultiRunlogDetail(runlogDetails))
                    .runWork(filterWorkCache.getWork(), new Dispatcher().setTmpDataMap(mapData));

            if (receiver != null) {
                response.addHeader(Constants.TRACKING_ID, receiver.getTrackingId());
                // 将执行过的所有 filter_trackingId 记录到 ctx 中去
                recordFilterStackData(filterWorkCache.getWork().getWorkName(), mutableRequest, receiver.getTrackingId());

                Map<String, Object> tmpDataMap = receiver.getTmpDataMap();
                if (tmpDataMap.containsKey(Constants.DO_ERROR_FILTER)) {
                    Map<String, ?> map = (Map<String, ?>) tmpDataMap.get(Constants.DO_ERROR_FILTER);
                    Object headerCode = map.get("headerCode");
                    status = DatatypeUtil.objectToInt(headerCode, 500);
                }
            }
        }
        return status;
    }

    private void recordFilterStackData(String filterWorkName, MutableHttpServletRequest mutableRequest, String trackingId) {
        String filterTrackingIds = mutableRequest.getHeader(Constants.FILTER_TRACKING_ID_STACK);
        filterTrackingIds = Optional.ofNullable(filterTrackingIds).orElse("");
        filterTrackingIds = String.format("%s,%s[<a onclick=\"window.showRunLogDetail('%s')\"><span style='color:blue;'>%s</span></a>]", filterTrackingIds, filterWorkName, trackingId, trackingId);
        filterTrackingIds = StringUtils.removeStart(filterTrackingIds, ",");

        mutableRequest.putHeader(Constants.FILTER_TRACKING_ID_STACK, filterTrackingIds);
    }

    private Map<String,Object> parseParam(WorkCache workCache, HttpServletRequest request, WorkStep workStep) {
        // 所有请求参数
        Map<String,Object> mapData = new HashMap<>();
        if (StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_START)) {
            Param.ParamInputSchema inputSchema = Param.getCacheParamInputSchema(workStep);
            List<Param.ParamInputSchemaItem> items = inputSchema.getParamInputSchemaItems();
            if (items != null) {
                items.forEach(item -> {
                    // 默认参数类型都当成 string 类型
                    mapData.put(item.getParamName(), request.getParameter(item.getParamName())); // 传递参数允许为空串
                });
            }
        }
        return mapData;
    }

    /**
     * 是否可以拦截请求
     * Filters 有两条记录,一条简单过滤器配置,一条复杂过滤器配置
     * @param filter
     * @return
     */
    private boolean canIntercept(String workName, Filters filter, HttpServletRequest request) {
        String workNameStr = filter.getWorkName();
        String complexWorkName = filter.getComplexWorkName();
        return canIntercept1(workName, workNameStr) || canIntercept2(workName, complexWorkName, request);

    }

    private boolean canIntercept2(String workName, String complexWorkName, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(complexWorkName)) {
            String[] complexWorkNameArr = StringUtils.splitByWholeSeparator(complexWorkName, ",");
            for (String _complexWorkName : complexWorkNameArr) {
                if (StringUtils.startsWith(_complexWorkName, workName)) {

                    // TODO
//                    return canInterceptWithParameter(_complexWorkName, request);
                }
            }

        }
        return false;
    }

    private boolean canIntercept1(String workName, String workNameStr) {
        String[] workNameArr = StringUtils.splitByWholeSeparator(workNameStr, ",");
        for (String _workName : workNameArr) {
            if (StringUtils.equals(_workName, workName)) {
                return true;
            }
        }
        return false;
    }
}
