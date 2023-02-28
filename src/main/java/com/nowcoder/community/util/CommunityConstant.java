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

    /**
     * 实体类型-帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型-评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型-用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题类型-评论
     */
    String TOPIC_COMENT = "comment";

    /**
     * 主题类型-点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题类型-关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题类型-发布帖子
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;
}
