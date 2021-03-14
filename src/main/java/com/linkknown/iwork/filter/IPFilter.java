package com.linkknown.iwork.filter;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 限制 iwork 框架访问 ip
 */
public class IPFilter implements Filter {

    private IworkConfig iworkConfig;
    public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
    private Set<String> excludesPattern;

    public IPFilter(IworkConfig iworkConfig) {
        this.iworkConfig = iworkConfig;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        String param = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (param != null && param.trim().length() != 0) {
            this.excludesPattern = new HashSet(Arrays.asList(param.split("\\s*,\\s*")));
        }
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURI = httpServletRequest.getRequestURI();

        if (!this.isExclusion(requestURI)) {
            if (isWhiteIp(httpServletRequest)) {
                filterChain.doFilter(request, response);
            } else {
                httpServletResponse.setStatus(401);
            }
        }else {
            // 不走过滤器
            System.out.println("===不进过滤器");
            filterChain.doFilter(request, response);
        }
    }

    private boolean isExclusion(final String requestURI) {
        if (this.excludesPattern == null) {
            // 不排除
            return false;
        } else {
            long count = this.excludesPattern.stream().filter(pattern -> ServletPathMatcher.matches(pattern, requestURI)).count();
            // count > 0 表示在排除列表里面
            return count > 0;
        }
    }

    private boolean isWhiteIp(HttpServletRequest httpServletRequest) {
        String ip = HttpUtil.getIpAddress(httpServletRequest);
        if (StringUtils.isNotBlank(iworkConfig.getWhiteIps()) && iworkConfig.getWhiteIps().contains(ip)) {
            return true;
        }
        return false;
    }
}
