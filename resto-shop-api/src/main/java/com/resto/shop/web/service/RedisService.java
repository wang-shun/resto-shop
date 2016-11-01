package com.resto.shop.web.service;
import com.resto.shop.web.constant.Function;
import redis.clients.jedis.ShardedJedis;

public interface RedisService {

       <T> T execute(Function<T, ShardedJedis> fun) ;

    /**
     * 执行set操作
     *
     * @param key
     * @param value
     * @return
     */
     String set(final String key, final String value);

    /**
     * 执行GET操作
     *
     * @param key
     * @return
     */
    String get(final String key);

    /**
     * 执行DEL操作
     *
     * @param key
     * @return
     */
     Long del(final String key);

    /**
     * 设置生存时间，单位为秒
     *
     * @param key
     * @param seconds
     * @return
     */
     Long expire(final String key, final Integer seconds);

    /**
     * 执行set操作并且设置生存时间，单位为秒
     *
     * @param key
     * @param value
     * @return
     */
    String set(final String key, final String value, final Integer seconds);




}
