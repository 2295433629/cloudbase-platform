package com.cloudbase.common.web.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Druid 监控配置
 * <p>
 * 开启 Druid 内置监控页面（/druid/index.html），支持：
 * <ul>
 *   <li>SQL 监控统计（含慢 SQL）</li>
 *   <li>URI 访问监控</li>
 *   <li>数据源连接池状态</li>
 * </ul>
 * 生产环境建议通过 {@code cloudbase.druid.allow} 限制访问 IP，
 * 或通过 {@code cloudbase.druid.enabled=false} 完全关闭。
 * </p>
 */
@Configuration
public class DruidConfig {

    @Value("${cloudbase.druid.enabled:true}")
    private boolean enabled;

    @Value("${cloudbase.druid.allow:}")
    private String allow;

    @Value("${cloudbase.druid.login-username:admin}")
    private String loginUsername;

    @Value("${cloudbase.druid.login-password:admin123}")
    private String loginPassword;

    /**
     * Druid 监控页面 Servlet
     * 访问地址：http://host:port/druid/index.html
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        if (!enabled) {
            return null;
        }
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> params = new HashMap<>();
        params.put("loginUsername", loginUsername);
        params.put("loginPassword", loginPassword);
        // IP 白名单，空则允许所有（生产环境务必限制）
        if (allow != null && !allow.isEmpty()) {
            params.put("allow", allow);
        }
        bean.setInitParameters(params);
        return bean;
    }

    /**
     * Druid Web 请求统计 Filter（用于 URI 监控）
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        if (!enabled) {
            return null;
        }
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());
        bean.addUrlPatterns("/*");
        // 排除静态资源和监控页面本身
        bean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        bean.addInitParameter("sessionStatEnabled", "true");
        bean.addInitParameter("profileEnabled", "true");
        return bean;
    }
}
