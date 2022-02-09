package com.sl.community.util;

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
 *
 *
 * @date 2021/12/9 14:52
 */

@Component
public class SensitiveFilter {

    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT="***";

    //根节点
    private TrieNode rootNode=new TrieNode();

    //首次访问时，初始化一次即可
    @PostConstruct  //在bean被初始化时调用，及服务启动时树构造完成
    public void init(){
        try (
                //读取配置文件的字符，构造前缀树
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");

                //字节流要关闭的，在try括号里创建对象，编译时会自动加入到finally里面关闭

                //将字节流转为字符流，缓冲流读取效率会高
                BufferedReader reader= new BufferedReader(new InputStreamReader(is));
                ){
            String keyword;
            while ((keyword=reader.readLine())!=null){
                //添加到前缀树
                this.addKeyWord(keyword);
            }

        } catch (Exception e) {
            //日志记录异常
            logger.error("过滤敏感词汇失败!"+e.getMessage());

        }



    }

    //将一个敏感词添加到前缀树中
    private void addKeyWord(String keyword) {
        //创建临时节点，
        TrieNode tempNode=rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            //试图获取子节点
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            //子节点为空
            if(subNode==null){
                //初始化子节点，并挂到当前节点下
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //将临时节点下移指向子节点
            tempNode=subNode;

            //设置结束标识
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /*
    * 过滤敏感词
    * 参数：待过滤的文本，  返回过滤后的文本
    * */
    public String filter(String text){
        // 参数空值判断
        if(StringUtils.isBlank(text)){
            return null;
        }
        //依赖三个指针去过滤
        TrieNode tempNode=rootNode; //指针一
        int begin=0;   //指针二
        int position=0;   //指针三

        //变量记录最后的结果，变长
        StringBuilder sb=new StringBuilder();

        // 利用指针三做条件
        while (begin<text.length()){
            if(position<text.length()){
                //得到指针三的字符
                char c = text.charAt(position);
                //跳过符号（跳过敏感词汇里面有符号）
                if(isSymbol(c)){
                    //若指针一属于根节点，将此符号计入结果，让指针2向下走
                    if(tempNode==rootNode){
                        begin++;
                        sb.append(c);
                    }
                    //无论符号在开头或中间，指针3都向下走一步
                    position++;
                    //跳过符号下面的业务就不用处理了，到此为止
                    continue;
                }
                //字符不是符号，检查下级节点
                //去下级节点
                tempNode = tempNode.getSubNode(c);

                //当下级没有节点，即以begin开头的字符串不是敏感词，将其添加到结果中
                if(tempNode==null){
                    sb.append(text.charAt(begin));
                    //指针2后移，且指针3与其保持一致
                    begin++;
                    position=begin;
                    //重新指向根节点
                    tempNode=rootNode;

                }else if(tempNode.isKeywordEnd()){//当当前节点是最后
                    //发现敏感词，将beagin~position字符串替换掉
                    sb.append(REPLACEMENT);
                    //position下移，并且指针2与其保持一致
                    position++;
                    begin=position;
                    //重新指向根节点
                    tempNode=rootNode;
                }else {
                    //检查下一个字符
                    if(position<text.length()-1){
                        position++;
                    }

                }

            }

        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /*
    * 判断是否为符号
    * */
    public boolean isSymbol(Character c){
        //利用工具，判断字符是不是普通的字符（abc。。），东亚文字范围中、日、韩文(0x2E80~0x9FFF)
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    /*
    定义前缀树
     */
    public class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd=false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

    //



}
