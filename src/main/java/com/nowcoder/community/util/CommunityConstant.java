package com.nowcoder.community.util;

/**
 * @author changzer
 * @date 2023/2/3
 * @apiNote
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATE_SUCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATE_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATE_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}