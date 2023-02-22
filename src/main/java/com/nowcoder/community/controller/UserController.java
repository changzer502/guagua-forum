package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author changzer
 * @date 2023/2/4
 * @apiNote
 */
@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domin;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常", e);
        }

        //更新当前用户头像的路径
        User user = hostHolder.getUser();
        String headerUrl = domin + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }


    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
        ) {
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取图片失败：" + e.getMessage());
            throw new RuntimeException("读取图片失败");
        }
    }

    @LoginRequired
    @PostMapping("/password")
    public String updatePassword(@CookieValue("ticket") String ticket, String oldPassword, String newPassword, Model model) {
        if (StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","请输入原始密码!");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","请输入新密码!");
            return "/site/setting";
        }
        //校验旧密码
        User user = hostHolder.getUser();
        String password = CommunityUtil.MD5(oldPassword + user.getSalt());
        if (password==null || !password.equals(user.getPassword())){
            model.addAttribute("oldPasswordMsg","旧密码错误！");
            return "/site/setting";
        }
        //更新数据库密码
        password = CommunityUtil.MD5(newPassword + user.getSalt());
        user.setPassword(password);
        userService.updatePassword(user.getId(), user.getPassword());
        //更新ticket状态
        userService.logout(ticket);
        return "redirect:/login";
    }



    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getprofilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalStateException("该用户不存在");
        }
        model.addAttribute("user",user);
        //点赞数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount",userLikeCount);
        return "/site/profile";
    }



}
