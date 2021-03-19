package com.github.rainhon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ADSL {
    /**
     * 执行CMD命令,并返回String字符串
     */
    public static String executeCmd(String strCmd) throws Exception {
        Process p = Runtime.getRuntime().exec("cmd /c " + strCmd);
        StringBuilder sbCmd = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
        String line;
        while ((line = br.readLine()) != null) {
            sbCmd.append(line + "\n");
        }
        return sbCmd.toString();

    }

    /**
     * 连接ADSL
     */
    public static boolean connAdsl(String adslTitle, String adslName, String adslPass) throws Exception {
        String adslCmd = "rasdial " + adslTitle + " " + adslName + " " + adslPass;
        String tempCmd = executeCmd(adslCmd);
        // 判断是否连接成功
        if (tempCmd.indexOf("已连接") > 0) {
            System.out.println("已成功建立连接.");
            return true;
        } else {
            System.err.println(tempCmd);
            System.err.println("建立连接失败");
            return false;
        }
    }

    /**
     * 断开ADSL
     */
    public static boolean cutAdsl(String adslTitle) throws Exception {
        String cutAdsl = "rasdial " + adslTitle + " /disconnect";
        String result = executeCmd(cutAdsl);

        if (result.indexOf("没有连接") != -1) {
            System.err.println(adslTitle + "连接不存在!");
            return false;
        } else {
            System.out.println("连接已断开");
            return true;
        }
    }
    /**
     * 测试网络是否连接
     */

    public static boolean isConnect(){
        boolean connect = false;
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec("ping " + "www.baidu.com");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("返回值为:"+sb);
            is.close();
            isr.close();
            br.close();

            if (null != sb && !sb.toString().equals("")) {
                String logString = "";
                if (sb.toString().indexOf("TTL") > 0) {
                    // 网络畅通
                    connect = true;
                } else {
                    // 网络不畅通
                    connect = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connect;
    }

    //测试代码
    public static void main(String[] args) throws InterruptedException,
            Exception { Scanner sc = new Scanner(System.in);
        System.out.println("宽带连接名称:"); //看你宽带连接的名称
        String name = sc.next();
        System.out.println("宽带账户:");
        String username = sc.next();
        System.out.println("宽带密码:");
        String password = sc.next();
        String adsl= "宽带连接";
        while(true){
            boolean connect = isConnect();
            Thread.sleep(100000);//单位毫秒,我设置的是100秒.自己看情况更改
            if(!connect){
                System.out.println("无网络,正在重新拨号");
                connAdsl(name,username,password);
            }
        }

    }
}
