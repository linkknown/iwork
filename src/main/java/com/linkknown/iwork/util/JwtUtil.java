package com.linkknown.iwork.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    /**
     * 创建 Token
     *
     * @param claimsMap 自己需要存储进 token 中的信息
     * @return token
     */
    public static String createJWT(String secretKey, HashMap<String, String> claimsMap, int expireSecond) {
        // 头部
        Map<String, Object> headerMap = new HashMap<>(4);
        // Headers
        // alg属性表示签名使用的算法,默认为HMAC SHA256(写为HS256);typ属性表示令牌的类型;JWT令牌统一写为JWT
        headerMap.put("alg", "HS256");
        headerMap.put("typ", "JWT");

        // 当前时间与过期时间
        Calendar time = Calendar.getInstance();
        Date now = time.getTime();
        time.add(Calendar.SECOND, expireSecond);
        Date expireTime = time.getTime();

        // 选择签名加密算法
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        // 创建 token 并返回
        return JWT.create().withHeader(headerMap)
                .withIssuedAt(now)
                .withExpiresAt(expireTime)
                .withSubject("subject")
                .withClaim("claim", claimsMap)
                .sign(algorithm);
    }

    /**
     * 验证、解析Token
     *
     * @param token 用户提交的token
     * @return 该token中的信息
     */
    public static Map<String, Object> verifyToken(String secretKey, String token) {
        DecodedJWT verifier = null;
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        try {
            verifier = JWT.require(algorithm).build().verify(token);
        } catch (Exception e) {
            throw e;
        }
        assert verifier != null;
        return verifier.getClaim("claim").asMap();
    }

}
