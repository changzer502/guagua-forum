package com.nowcoder.community.controller;

import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    //@LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    //@LoginRequired
    @RequestMapping(path = "/profile", method = RequestMethod.GET)
    public String getprofilePage() {
        return "/site/profile";
    }

    //@LoginRequired
    @RequestMapping(path = "/letter", method = RequestMethod.GET)
    public String getletterPage() {
        return "/site/letter";
    }

}
