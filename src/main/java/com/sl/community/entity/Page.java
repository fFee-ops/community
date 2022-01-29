package com.sl.community.entity;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/7 10:25
 */
/*封装分页相关信息,页面传入，服务器查询，计算得到*/
public class Page {
    //当前页码
    private int current=1;
    //显示上限
    private int limit=10;
    //数据总数（用于计算总页数）
    private int rows;
    //查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        //判断一下用户输入的页码是否合理
        if(current>=1){
            this.current = current;
        }

    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //限制一页显示的最大数
        if(limit>=1 && limit<=100){
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0){
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /*获取当前页的起始行*/
    public int getoffset(){
        //current*limit-limit
        return (current-1)*limit;
    }
    //获取总页数
    public int getTotal(){
        if(rows%limit==0){
            return rows/limit;
        }else {
            return rows/limit +1;
        }
    }
    //获取起始页码（当前页离他最近的2页）
    public int getFrom(){
        int from=current-2;
        return from>1 ? from:1;
    }
    //获取终止页码
    public int getTo(){
        int to=current+2;
        int total=getTotal();
        return to>total?total:to;
    }
}
