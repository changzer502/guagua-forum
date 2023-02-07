package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author changzer
 * @date 2023/2/7
 * @apiNote
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换字符
    private static final String REPLACE_KEY = "***";
    //根节点
    private TrieNode root = new TrieNode();

    //前缀树
    private class TrieNode {
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    @PostConstruct
    public void init() {
        try (
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String keyWord;
            while((keyWord = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyWord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将一个敏感词添加到前缀树去
    private void addKeyword(String keyWord) {
        TrieNode tempNode = root;
        for(int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点，进入下一次循环
            tempNode = subNode;

            //设置结束标识
            if (i == keyWord.length()-1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter (String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode tempNode = root;

        int begin = 0;
        int position = 0;

        StringBuilder sb = new StringBuilder();
        while(position < text.length()) {
            char c = text.charAt(position);

            //跳过特殊字符
            if (isSymbol(c)){
                //若tempNode处于根节点，此符号计入结果，position向下走一步
                if (tempNode == root){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检测下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                //以begin开始的字符不是敏感词
                sb.append(c);
                //进入下一个位置
                position = ++begin;
                tempNode = root;
            }else if (tempNode.isKeywordEnd()) {
                //发现敏感词，将begin~position字符串替换
                sb.append(REPLACE_KEY);
                //进入下一个位置
                begin = ++position;
                tempNode = root;
            }else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol (Character c){
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}
