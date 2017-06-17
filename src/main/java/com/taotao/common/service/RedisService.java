package com.taotao.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by JARVIS on 2017/6/14.
 */

@Service
public class RedisService {

    @Autowired(required = false) //从spring容器去找改bean找到就注入，找不到就忽略
    private ShardedJedisPool shardedJedisPool;

    /**
     * 方法的抽取，利用匿名内部类
     *
     * @param fun
     * @param <T>
     * @return
     */
    private <T> T execute(Function<T, ShardedJedis> fun) {
        ShardedJedis shardedJedis = null;
        try {
            // 从连接池中获取到jedis分片对象
            shardedJedis = shardedJedisPool.getResource();
            return fun.callback(shardedJedis);
        } finally {
            if (null != shardedJedis) {
                // 关闭，检测连接是否有效，有效则放回到连接池中，无效则重置状态
                shardedJedis.close();
            }
        }
    }


    /**
     * 执行set操作
     * @param key
     * @param value
     * @return
     */
    /*public String set(String key,String value) {
        ShardedJedis shardedJedis = null;
        try {
            // 从连接池中获取到jedis分片对象
            shardedJedis = shardedJedisPool.getResource();
            return shardedJedis.set(key, value);
        } finally {
            if (null != shardedJedis) {
                // 关闭，检测连接是否有效，有效则放回到连接池中，无效则重置状态
                shardedJedis.close();
            }
        }
    }*/

    /**
     * 优化后的 set 方法
     *
     * @param key
     * @param value
     * @return
     */
    public String set(final String key, final String value) {
        return this.execute(new Function<String, ShardedJedis>() {
            @Override
            public String callback(ShardedJedis shardedJedis) {
                return shardedJedis.set(key, value);
            }
        });
    }

    /**
     * 执行get操作
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        return this.execute(new Function<String, ShardedJedis>() {
            @Override
            public String callback(ShardedJedis shardedJedis) {
                return shardedJedis.get(key);
            }
        });
    }

    /**
     * 执行删除操作
     *
     * @param key
     * @return
     */
    public Long del(final String key) {
        return this.execute(new Function<Long, ShardedJedis>() {
            @Override
            public Long callback(ShardedJedis shardedJedis) {
                return shardedJedis.del(key);
            }
        });
    }

    /**
     * 设置生存时间
     *
     * @param key
     * @param seconds
     * @return
     */
    public Long expire(final String key, final int seconds) {
        return this.execute(new Function<Long, ShardedJedis>() {
            @Override
            public Long callback(ShardedJedis shardedJedis) {
                return shardedJedis.expire(key, seconds);
            }
        });
    }

    /**
     * 执行set操作，并设置生存时间，单位为秒
     *
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public String set(final String key, final String value, final int seconds) {
        return this.execute(new Function<String, ShardedJedis>() {
            @Override
            public String callback(ShardedJedis shardedJedis) {
                String str = shardedJedis.set(key, value);
                shardedJedis.expire(key, seconds);
                return str;
            }
        });
    }

}
