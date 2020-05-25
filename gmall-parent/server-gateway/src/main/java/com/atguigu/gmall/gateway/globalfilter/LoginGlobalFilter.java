package com.atguigu.gmall.gateway.globalfilter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Administrator
 * @create 2020-05-24 20:18
 */
@Component
public class LoginGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${auth.url}")
    private String[] authUrl;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //请求路径中对inner的请求进行拦截
        String path = request.getURI().getPath();
        if (antPathMatcher.match("/inner/**",path)){
            return returnResult(response,ResultCodeEnum.PERMISSION);
        }

        //对路径中包含auth的进行拦截

        //获取临时用户id
        String userTempId = getUserTempId(request);
        if (!StringUtils.isEmpty(userTempId)){
            request.mutate().header("userTempId", userTempId);
        }
        //获取用户id
        String userId = getUserId(request);
        if (antPathMatcher.match("/auth/**",path)){
            if (StringUtils.isEmpty(userId)) {
                return returnResult(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }
        for (String url : authUrl) {
            if (path.contains(url)&&StringUtils.isEmpty(userId)){
                try {
                    response.getHeaders().set(HttpHeaders.LOCATION,
                            "http://login.gmall.com//login.html?originUrl="+
                                    URLEncoder.encode(request.getURI().getRawSchemeSpecificPart(),"utf-8"));
                    return response.setComplete();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!StringUtils.isEmpty(userId)) {
            request.mutate().header("userId", userId);
        }
        return chain.filter(exchange);
    }


    //抽取共同部分
    private Mono<Void> returnResult(ServerHttpResponse response,ResultCodeEnum resultCodeEnum){
        String result = JSONObject.toJSONString(Result.build(null,resultCodeEnum));
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer wrap = dataBufferFactory.wrap(result.getBytes());
        //进行编码的设置
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE,"application/json;charset=utf-8");
        return response.writeWith(Mono.just(wrap));
    }

    //获取id的方法
    private String getUserTempId(ServerHttpRequest request) {
        //先从请求头中获取
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (StringUtils.isEmpty(userTempId)){
            //如果请求头中没有再从cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if (null != httpCookie){
                userTempId = httpCookie.getValue();
            }
        }
        return userTempId;
    }

    //获取id的方法
    private String getUserId(ServerHttpRequest request) {
        //先从请求头中获取
        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)){
            //如果请求头中没有再从cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if (null != httpCookie){
                token = httpCookie.getValue();
            }
        }
        if (!StringUtils.isEmpty(token)){
            if (redisTemplate.hasKey(token)) {
                 return  (String) redisTemplate.opsForValue().get(token);
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
