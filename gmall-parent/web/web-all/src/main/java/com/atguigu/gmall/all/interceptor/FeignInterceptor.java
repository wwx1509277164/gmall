package com.atguigu.gmall.all.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-25 19:48
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    //在远程调用之前的请求进行处理的方法

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //将当前微服务中的请求头中的真实用户的Id 临时用户的Id存放到拦截器当中

       ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
       if (servletRequestAttributes!=null) {
           HttpServletRequest request = servletRequestAttributes.getRequest();
           if (request!=null){
               String userId = request.getHeader("userId");
               System.out.println(userId);
               if (!StringUtils.isEmpty(userId)){
                    requestTemplate.header("userId",userId);
               };
               String userTempId = request.getHeader("userTempId");
               if (!StringUtils.isEmpty(userTempId)){
                   requestTemplate.header("userTempId",userTempId);
               };
           }
       }
    }
}
