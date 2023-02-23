package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author changzer
 * @date 2023/2/22
 * @apiNote
 */
@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId,entityType);
                redisOperations.multi();
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                long l = System.currentTimeMillis();
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    //取关
    public void unFollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId,entityType);
                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);

                return redisOperations.exec();
            }
        });
    }

    //查询某个目标关注的实体数量
    public long fingFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询某个实体的粉丝数量
    public long fingFollowerCount(int entityId, int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(entityId,entityType);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注当前实体
    public boolean hasFollowed(int userId, int entityId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, limit + offset - 1);
        if (targetIds.isEmpty()){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            Map<String, Object> map = new HashMap<>();
            User userById = userService.findUserById(id);
            map.put("user", userById);
            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, limit + offset - 1);
        if (targetIds.isEmpty()){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            Map<String, Object> map = new HashMap<>();
            User userById = userService.findUserById(id);
            map.put("user", userById);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
