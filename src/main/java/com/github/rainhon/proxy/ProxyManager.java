package com.github.rainhon.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.rainhon.Controller;
import com.github.rainhon.Main;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProxyManager {
    List<AProxy> AProxyList = new ArrayList<>();
    String APIUrl = "http://webapi.http.zhimacangku.com/getip?num=10&type=2&pro=&city=0&yys=0&port=1&pack=139606&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=";

    public void getProxies(){
        HttpClient client = new HttpClient();
        try{
            Controller controller = Main.getLoader().getController();
            APIUrl = controller.getProxyAPI();

            if(APIUrl.equals("")){
                throw new Exception("代理连接不能未空");
            }
            HttpMethod method = new GetMethod(APIUrl);
            client.executeMethod(method);
            String result = method.getResponseBodyAsString();
            JSONObject response = JSON.parseObject(result);
            JSONArray data = response.getJSONArray("data");
            for (Object o : data) {
                JSONObject proxy = (JSONObject) o;
                AProxyList.add(new AProxy(proxy.getString("ip"), proxy.getInteger("port"), proxy.getString("expire_time")));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void init(){
        loadProxies();
        getProxies();
    }

    public AProxy getProxy(int tryTimes) throws Exception{
        if(tryTimes > AProxyList.size()){
            throw new Exception("无可用代理");
        }
        AProxy proxy = AProxyList.get((int)(Math.random() * AProxyList.size()));
        if(!proxy.canConnect()){
            return getProxy(++tryTimes);
        }else{
            return proxy;
        }
    }

    public void saveProxies(){
        try(Writer writer = new FileWriter("../data/proxies.txt");){
            for(AProxy p : AProxyList){
                if(p.canConnect){
                    writer.write(p.getIp() + ',' + p.getPort() + "\n");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void loadProxies(){
        try(Reader reader = new FileReader("../data/proxies.txt");
            BufferedReader bufferedReader = new BufferedReader(reader);
        ){
            String line;
            while((line = bufferedReader.readLine()) != null){
                String[] temp = line.split(",");
                AProxyList.add(new AProxy(temp[0], Integer.parseInt(temp[1])));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
