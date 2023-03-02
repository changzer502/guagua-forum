package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "E:/学习/tools/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com E:/学习/tools/wkhtmltopdf/wk_img/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
