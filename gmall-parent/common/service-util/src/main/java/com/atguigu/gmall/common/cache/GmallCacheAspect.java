package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存切面实现类
 */
@Aspect
@Component
@Slf4j
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;//此Redis 保存Value值的时候 此Value要求必须实现序列化接口
    @Autowired
    private RedissonClient redissonClient;//上锁

    //方法  完成缓存的方法
    @Around(value = "@annotation(com.atguigu.gmall.common.cache.GmallCache)")//进入此切面方法的条件
    public Object cacheAspectMethod(ProceedingJoinPoint proceedingJoinPoint){
        Object[] args = proceedingJoinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Class returnType = methodSignature.getReturnType();
        String prefix = methodSignature.getMethod().getAnnotation(GmallCache.class).prefix();
        String cacheKey = prefix+Arrays.asList(args).toString();
        Object object = redisTemplate.opsForValue().get(cacheKey);
        if (object!=null){
            return object;
        }
        String cacheKeyLock = cacheKey+RedisConst.SKULOCK_SUFFIX;
        RLock lock = redissonClient.getLock(cacheKeyLock);
        try {
            boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            if (res){
                Object returnValue = proceedingJoinPoint.proceed(args);
                if (returnValue==null){
                    returnValue = returnType.newInstance();
                    redisTemplate.opsForValue().set(cacheKey,returnValue,5,TimeUnit.MINUTES);
                    return returnValue;
                }else {
                    redisTemplate.opsForValue().set(cacheKey,returnValue,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    return returnValue;
                }
            }else {
                Thread.sleep(2000);
                log.info("已经有人获取到锁了");
                return redisTemplate.opsForValue().get(cacheKey);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }
}
