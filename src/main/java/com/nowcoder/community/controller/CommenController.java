package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author changzer
 * @date 2023/2/19
 * @apiNote
 */
@Controller
@RequestMapping("/comment")
public class CommenController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable int discussPostId, Comment comment) {
        int userId = hostHolder.getUser().getId();
        comment.setUserId(userId);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMENT)
                .setUserId(userId)
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost discussionPost = discussPostService.findDiscussionPost(comment.getEntityId());
            event.setUserId(discussionPost.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
