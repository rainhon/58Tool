package com.github.rainhon.proxy;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AProxy {
    @JSONField
    String ip;
    @JSONField
    int port;
    @JSONField
    String expireTimeString;

    boolean canConnect = false;

    Date expireTime;
    //"2021-03-17 23:27:16
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AProxy(String ip, int port){
        this.ip=ip;
        this.port=port;
    }

    public AProxy(String ip, int port, String expireTimeString){
        this.ip=ip;
        this.port=port;
        this.expireTimeString=expireTimeString;
        try{
            expireTime = dateFormat.parse(expireTimeString);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getExpireTimeString() {
        return expireTimeString;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setExpireTimeString(String expireTimeString) {
        this.expireTimeString = expireTimeString;
    }

    public boolean canConnect(){
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        try{
            URLConnection conn= new URL("https://baidu.com").openConnection(proxy);
            conn.setConnectTimeout(1000);
            conn.getInputStream();
            this.canConnect = true;
        }catch(IOException e){
            this.canConnect = false;
        }
        return canConnect;
    }
}
