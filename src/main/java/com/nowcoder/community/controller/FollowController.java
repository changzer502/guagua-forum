package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author changzer
 * @date 2023/2/22
 * @apiNote
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityId, int entityType){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityId, int entityType){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User userById = userService.findUserById(userId);
        if (userById == null){
            throw new IllegalStateException("该用户不存在！");
        }
        model.addAttribute("user", userById);

        page.setLimit(5);
        page.setRows((int)followService.fingFolloweeCount(userId, ENTITY_TYPE_USER));
        page.setPath("/followees/"+userId);
        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());

        if (followees != null && !followees.isEmpty()){
            for (Map<String, Object> map:followees){
                User user = (User)map.get("user");
                map.put("hasFollow",hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followees);
        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User userById = userService.findUserById(userId);
        if (userById == null){
            throw new IllegalStateException("该用户不存在！");
        }
        model.addAttribute("user", userById);

        page.setLimit(5);
        page.setRows((int)followService.fingFollowerCount(userId, ENTITY_TYPE_USER));
        page.setPath("/followers/"+userId);
        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());

        if (followers != null && !followers.isEmpty()){
            for (Map<String, Object> map:followers){
                User user = (User)map.get("user");
                map.put("hasFollow",hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followers);
        return "/site/follower";
    }

    public boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), userId, ENTITY_TYPE_USER);
    }
}
