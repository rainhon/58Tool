package com.github.rainhon.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class IPManager {
    private String currentIP;
    private String title;
    private String name;
    private String password;
    private final HttpClient httpClient = new HttpClient();
    private final String getIPUrl = "http://ip.3322.net/";
    private int reconnectTimes;

    public IPManager(String title, String name, String password, int reconnectTimes){
        this.title = title;
        this.name = name;
        this.password = password;
        this.reconnectTimes = reconnectTimes;
    }

    public void reconnect() throws Exception{
        ADSL.cutAdsl(title);
        ADSL.connAdsl(title, name ,password);
        String newIP = getIP();
        if(newIP.equals(currentIP)){
            Thread.sleep(1000);
            reconnect();
        }else{
            currentIP = newIP;
        }
    }

    public String getIP(){
        HttpMethod method = new GetMethod(getIPUrl);
        String result  = "";
        try{
            httpClient.executeMethod(method);
            result = method.getResponseBodyAsString();
        }catch (Exception e){
            System.out.println("无法获取外网IP");
            System.out.println(e.getMessage());
        }

        return result;
    }

    public int getReconnectTimes(){
        return reconnectTimes;
    }
}
