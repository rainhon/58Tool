package com.github.rainhon;

import com.alibaba.fastjson.JSON;
import com.github.rainhon.responseData.PhoneData;
import com.github.rainhon.responseData.ResultData;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAPI {
    private String key = "";
    private final HttpClient client = new HttpClient();
    private final String domain = "http://www.fggdd.cn/";
    private String mod = "sj";
    private String item = "";
    private String account = "";
    private String password = "";

    MessageAPI(String account, String password, String item) throws Exception{
        this.account = account;
        this.password = password;
        this.item = item;
        getKey();
    }

    public PhoneRecord getPhoneNumber() throws Exception {
        HttpMethod method = new GetMethod(domain + "/api/index/getPhone");
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("key", key),
                new NameValuePair("item", item),
                new NameValuePair("mode", mod)
        });

        ResultData resultData = doRequest(method);
        if(resultData.code == 1){
            PhoneData phonedata = JSON.parseObject(resultData.data, PhoneData.class);
            return new PhoneRecord(phonedata.phone, PhoneRecord.PhoneStatus.FRESH, phonedata.phoneId);
        }else{
            throw new Exception(resultData.getMsg());
        }
    }

    public void banPhone(PhoneRecord phoneRecord) throws Exception {
        HttpMethod method = new GetMethod(domain + "/api/index/banPhone");
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("key", key),
                new NameValuePair("phoneId", String.valueOf(phoneRecord.getPhoneId())),
        });

        ResultData resultData = doRequest(method);
        if(resultData.code == 1){
            phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.BLACK);
        }else{
            throw new Exception(resultData.getMsg());
        }
    }

    public String readMessage(PhoneRecord phoneRecord) throws Exception{
        String validateCode = "";
        HttpMethod method = new GetMethod(domain + "/api/index/getSms");
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("key", key),
                new NameValuePair("phoneId", String.valueOf(phoneRecord.getPhoneId())),
        });
        ResultData resultData = doRequest(method);
        if(resultData.code == 1){
            String message = resultData.data;

            Pattern pattern = Pattern.compile("\\d{6}");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                return matcher.group();
            }else{
                throw new Exception("短信中没有验证码");
            }
        }else{
            throw new Exception(resultData.getMsg());
        }
    }

    public void getKey() throws Exception{
        HttpMethod method = new GetMethod(domain + "/api/index/login");
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("user", account),
                new NameValuePair("pass", password),
        });
        ResultData resultData = doRequest(method);
        if(resultData.code == 1) {
            key = resultData.data;
        }else{
            throw new Exception("获取账号失败"+resultData.msg);
        }
    }

    public ResultData doRequest(HttpMethod method){
        String response = "";
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            response = new String(responseBody);
            System.out.println(response);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        }

        return JSON.parseObject(response, ResultData.class);
    }
}
