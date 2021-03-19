package com.github.rainhon.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.rainhon.responseData.CookieJson;
import org.openqa.selenium.Cookie;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UtilCookie {
    public static Set<Cookie> parseCookieString(String CookieString){
        Set<Cookie> cookieSet = new HashSet<>();
        String[] cookiesStringArray = CookieString.split(";");
        Cookie cookie;
        for (String s : cookiesStringArray) {
            CookieJson ck = JSON.parseObject(s, CookieJson.class);
            Cookie.Builder builder = new Cookie.Builder(ck.getName(), ck.getValue());

            if(ck.getDomain() != null){
                builder.domain(ck.getDomain());
            }

            if(ck.getExpiry() != null){
                builder.expiresOn(new Date(ck.getExpiry()));
            }

            if(ck.getHttpOnly() != null){
                builder.isHttpOnly(Boolean.getBoolean(ck.getHttpOnly()));
            }

            if(ck.getSecure() != null){
                builder.isSecure(Boolean.getBoolean(ck.getSecure()));
            }

            if(ck.getPath() != null){
                builder.path(ck.getPath());
            }
            cookie = builder.build();
            cookieSet.add(cookie);
        }
        return cookieSet;
    }

    public static String buildCookieJsonString(Set<Cookie> cookieSet){
        if(cookieSet.isEmpty()){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (Cookie cookie : cookieSet) {
            stringBuilder.append(JSONObject.toJSONString(cookie)).append(";");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    public static String buildCookieString(Set<Cookie> cookieSet){
        if(cookieSet.isEmpty()){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (Cookie cookie : cookieSet) {
            stringBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
