package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author changzer
 * @date 2023/2/22
 * @apiNote
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityId, int entityType){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityId, int entityType){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }
}
