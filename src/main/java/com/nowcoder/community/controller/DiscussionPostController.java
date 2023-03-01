package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author changzer
 * @date 2023/2/7
 * @apiNote
 */
@Controller
@RequestMapping("/discuss")
public class DiscussionPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussion(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录哦");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussionPost(discussPost);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussionPost(@PathVariable("discussPostId") int discussionPostId, Model model, Page page){
        //帖子
        DiscussPost discussionPost = discussPostService.findDiscussionPost(discussionPostId);
        model.addAttribute("post",discussionPost);
        //作者
        User user = userService.findUserById(discussionPost.getUserId());
        model.addAttribute("user",user);
        //点赞
        //点赞数量
        long likeCount = likeService.findEntityCount(ENTITY_TYPE_POST, discussionPostId);
        model.addAttribute("likeCount",likeCount);
        User loginUser = hostHolder.getUser();
        //点赞状态
        if (loginUser != null){
            int likeStatus = likeService.findEntityStatus(ENTITY_TYPE_POST, discussionPostId, loginUser.getId());
            model.addAttribute("likeStatus",likeStatus);
        }else {
            model.addAttribute("likeStatus",0);
        }


        //评论
        //设置分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussionPostId);
        page.setRows(discussionPost.getCommentCount());

        List<Comment> commentsByEntity = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussionPost.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentsByEntity != null){
            for (Comment comment : commentsByEntity){
                Map<String, Object> commentVo = new HashMap<>();
                User commentUser = userService.findUserById(comment.getUserId());
                commentVo.put("comment",comment);
                commentVo.put("user",commentUser);
                //点赞数量
                likeCount = likeService.findEntityCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                loginUser = hostHolder.getUser();
                if (loginUser != null){
                    int likeStatus = likeService.findEntityStatus(ENTITY_TYPE_COMMENT, comment.getId(), loginUser.getId());
                    commentVo.put("likeStatus",likeStatus);
                }else {
                    commentVo.put("likeStatus",0);
                }
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply : replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        loginUser = hostHolder.getUser();
                        if (loginUser != null){
                            int likeStatus = likeService.findEntityStatus(ENTITY_TYPE_COMMENT, reply.getId(), loginUser.getId());
                            replyVo.put("likeStatus",likeStatus);
                        }else {
                            replyVo.put("likeStatus",0);
                        }
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }


    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }


    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }


    //删除
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        //触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
