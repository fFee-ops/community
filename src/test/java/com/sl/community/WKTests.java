package com.sl.community;

import java.io.IOException;

public class WKTests {
    public static void main(String[] args) {
        String cmd="C:/user/soft/wk/wkhtmltopdf/bin/wkhtmltoimage --quality  75  https://www.nowcoder.com  C:/nowcoder_community/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
