package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
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
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId){
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId);
        //数量
        long entityCount = likeService.findEntityCount(entityType, entityId);
        //状态
        int entityStatus = likeService.findEntityStatus(entityType, entityId, user.getId());
        Map<String,Object> map=new HashMap<>();
        map.put("likeStatus", entityStatus);
        map.put("likeCount", entityCount);

        return CommunityUtil.getJSONString(0,null,map);

    }
}
