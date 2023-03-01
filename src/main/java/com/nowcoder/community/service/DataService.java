package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author changzer
 * @date 2023/3/1
 * @apiNote
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    //将指定的ip计入UV
    public void recordUV(String ip){
        String uvKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整理范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String uvKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE,1);
        }

        //合并这些数据
        String uvKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey,keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }


    //将指定用户计入DAU
    public void recordDAU(int userId){
        String dauKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId,true);
    }

    //统计指定范围日期内的DAU
    public long calculateDAU(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整理范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String uvKey = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(uvKey.getBytes());
            calendar.add(Calendar.DATE,1);
        }

        //进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        });
    }
}
