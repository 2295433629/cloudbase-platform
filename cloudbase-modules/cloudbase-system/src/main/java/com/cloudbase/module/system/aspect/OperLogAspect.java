package com.cloudbase.module.system.aspect;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysOperLog;
import com.cloudbase.module.system.mapper.SysOperLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 操作日志切面 - @Log注解驱动，同步存储
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperLog logRecord = new SysOperLog();
        // logId 由 MyBatis-Plus ASSIGN_ID（雪花算法）自动生成，避免毫秒级重复

        try {
            // 安全获取 HTTP 请求（非 Web 上下文时可能为 null）
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logRecord.setOperIp(getClientIp(request));
            }
            logRecord.setModule(logAnnotation.title());
            logRecord.setOperType(logAnnotation.businessType().name());

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            logRecord.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            if (logAnnotation.isSaveRequestData()) {
                try {
                    // 过滤不可序列化的参数（如 HttpServletRequest/Response）
                    Object[] args = joinPoint.getArgs();
                    Object[] safeArgs = new Object[args.length];
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof HttpServletRequest
                                || args[i] instanceof HttpServletResponse) {
                            safeArgs[i] = args[i].getClass().getSimpleName();
                        } else {
                            safeArgs[i] = args[i];
                        }
                    }
                    String params = objectMapper.writeValueAsString(safeArgs);
                    logRecord.setRequestParam(StringUtils.substring(params, 0, 2000));
                } catch (Exception e) {
                    log.warn("序列化请求参数失败: {}", e.getMessage());
                }
            }

            // 在切面内提前获取用户信息（避免 ThreadLocal 在异步场景下丢失）
            Long userId = UserContext.getUserId();
            String username = UserContext.getUsername();
            if (userId != null) {
                logRecord.setOperUserId(userId);
                logRecord.setOperUserName(username);
            }

            Object result = joinPoint.proceed();

            logRecord.setSuccess(1);
            logRecord.setCostTime(System.currentTimeMillis() - startTime);

            if (logAnnotation.isSaveResponseData() && result != null) {
                try {
                    String resp = objectMapper.writeValueAsString(result);
                    logRecord.setResponseResult(StringUtils.substring(resp, 0, 2000));
                } catch (Exception e) {
                    log.warn("序列化响应结果失败: {}", e.getMessage());
                }
            }

            saveLog(logRecord);
            return result;

        } catch (Throwable e) {
            logRecord.setSuccess(0);
            logRecord.setResponseResult(StringUtils.substring(e.getMessage(), 0, 500));
            logRecord.setCostTime(System.currentTimeMillis() - startTime);
            saveLog(logRecord);
            throw e;
        }
    }

    /**
     * 同步保存日志（@Async 在同类内部调用时不走代理，改为同步更可靠）
     */
    private void saveLog(SysOperLog logRecord) {
        try {
            logRecord.setOperTime(LocalDateTime.now());
            operLogMapper.insert(logRecord);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
