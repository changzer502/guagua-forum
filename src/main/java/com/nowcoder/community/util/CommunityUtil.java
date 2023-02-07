package com.nowcoder.community.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author changzer
 * @date 2023/2/3
 * @apiNote
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    public static String MD5(String key){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String,Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张氏");
        map.put("age", 29);
        System.out.println(getJSONString(200,"ok", map));
    }
}
