package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author changzer
 * @date 2023/2/22
 * @apiNote
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //数量
        long entityCount = likeService.findEntityCount(entityType, entityId);
        //状态
        int entityStatus = likeService.findEntityStatus(entityType, entityId, user.getId());
        Map<String,Object> map=new HashMap<>();
        map.put("likeStatus", entityStatus);
        map.put("likeCount", entityCount);

        //触发点赞事件
        if (entityStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityId(entityId)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        return CommunityUtil.getJSONString(0,null,map);

    }
}
