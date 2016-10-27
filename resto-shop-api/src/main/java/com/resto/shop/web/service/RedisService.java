package com.resto.shop.web.service;
import com.resto.shop.web.constant.Function;
import org.springframework.beans.factory.annotation.Autowired;

public interface RedisService {



    public static  <T> T execute(Function<T, Object> fun) {
        return null;
    }

    /**
     * 执行set操作
     *
     * @param key
     * @param value
     * @return
     */
    public String set(final String key, final String value) {
        return null;
    }

    /**
     * 执行GET操作
     *
     * @param key
     * @return
     */
    public String get(final String key) {
       return null;
    }

    /**
     * 执行DEL操作
     *
     * @param key
     * @return
     */
    public Long del(final String key) {
        return null;
    }

    /**
     * 设置生存时间，单位为秒
     *
     * @param key
     * @param seconds
     * @return
     */
    public Long expire(final String key, final Integer seconds) {
        return null;
    }

    /**
     * 执行set操作并且设置生存时间，单位为秒
     *
     * @param key
     * @param value
     * @return
     */
    public  String set(final String key, final String value, final Integer seconds) {
        return null;
    }



}
