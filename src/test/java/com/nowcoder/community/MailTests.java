package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

/**
 * @author changzer
 * @date 2023/2/3
 * @apiNote
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMail() {
        mailClient.sendMail("2784247628@qq.com", "tt", "tt");
    }

    @Test
    public void testTemplateEngine() {
        Context context = new Context();
        context.setVariable("username", "changzer");

        context.setVariable("email", "2784247628@qq.com");

        String url = "http://localhost" + "/activation/" + "/" + UUID.randomUUID().toString();
        context.setVariable("url", url);

        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail("2784247628@qq.com", "激活账户", process);

    }
}
