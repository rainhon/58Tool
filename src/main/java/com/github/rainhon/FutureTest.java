package com.github.rainhon;

import com.alibaba.fastjson.JSON;
import com.github.rainhon.responseData.CookieJson;
import com.github.rainhon.util.UtilCookie;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import java.io.FileWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class FutureTest {
    static Future<?> future;
    static ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    static int count=0;
    public static void main(String[] args) throws Exception{
        ExecutorService service = Executors.newScheduledThreadPool(10);
        Future<?> future1 = service.submit(()->{
            try{
                System.in.read();
            }catch (Exception e){

            }

        });


    }
}
