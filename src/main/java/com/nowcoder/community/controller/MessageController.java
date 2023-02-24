package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author changzer
 * @date 2023/2/19
 * @apiNote
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null){
            for (Message message : conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                int targetId = user.getId() == message.getFromId()? message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询未读信息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getConversationDetail(@PathVariable String conversationId, Model model, Page page){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //失信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<Map<String,Object>>();
        List<Integer> ids = new ArrayList<>();
        if (letterList != null){
            for (Message message:letterList){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);

                //获得未读消息的id
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        model.addAttribute("letters",letters);

        model.addAttribute("target",getLetterTarget(conversationId));
        return "/site/letter-detail";

    }

    public User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content){
        User toUser = userService.getUserByUsername(toName);
        if (toUser == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(toUser.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        //查询评论类的通知
        Map<String, Object> commentNotice = getMessageByTopic(user.getId(), TOPIC_COMENT);
        model.addAttribute("commentNotice",commentNotice);

        //查询点赞类的通知
        Map<String, Object> likeNotice = getMessageByTopic(user.getId(), TOPIC_LIKE);
        model.addAttribute("likeNotice",likeNotice);
        //查询关注类的通知
        Map<String, Object> followNotice = getMessageByTopic(user.getId(), TOPIC_FOLLOW);
        model.addAttribute("followNotice",followNotice);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);


        return "/site/notice";
    }
    public Map<String, Object> getMessageByTopic(int userId, String topic){
        Message message = messageService.selectLastestNotice(userId, topic);
        Map<String,Object> messageVo = new HashMap<>();
        messageVo.put("message", message);
        if (message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            if (topic != TOPIC_FOLLOW){
                messageVo.put("postId", data.get("postId"));
            }
            int commentCount = messageService.selectNoticeCount(userId, topic);
            int commentUnreadCount = messageService.selectNoticeUnreadCount(userId, topic);
            messageVo.put("count", commentCount);
            messageVo.put("unread", commentUnreadCount);
        }
        return messageVo;
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable String topic, Page page, Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setRows(messageService.selectNoticeCount(user.getId(), topic));
        page.setPath("/notice/detail/"+topic);

        List<Message> messages = messageService.selectMessages(user.getId(), topic, page.getOffset(), page.getLimit());

        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if (noticeVoList != null){
            for (Message notice : messages){
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                Map<String,Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(notice.getContent()), HashMap.class);
                map.put("user",userService.findUserById((Integer)data.get("userId")));
                map.put("postId",data.get("postId"));
                map.put("entityType",data.get("entityType"));
                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                ids.add(notice.getId());
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
