package com.nowcoder.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author changzer
 * @date 2023/3/2
 * @apiNote
 */
@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String storage;

    @PostConstruct
    public void init() {
        File wkImage = new File(storage);
        if (!wkImage.exists()) {
            wkImage.mkdirs();
            logger.info("创建WK图片目录："+storage);
        }
    }
}
