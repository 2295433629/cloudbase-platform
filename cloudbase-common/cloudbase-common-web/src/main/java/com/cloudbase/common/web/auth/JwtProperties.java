package com.cloudbase.common.web.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudbase.jwt")
public class JwtProperties {

    /** JWT签名密钥 */
    private String secret = "CloudBase@2026!JWTSecretKeyForTokenSigning";

    /** Token过期时间（秒），默认2小时 */
    private long expiration = 7200;

    /** Token请求头名称 */
    private String header = "Authorization";

    /** Token前缀 */
    private String tokenPrefix = "Bearer ";
}
