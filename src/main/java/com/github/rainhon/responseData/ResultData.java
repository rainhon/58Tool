package com.github.rainhon.responseData;

import com.alibaba.fastjson.annotation.JSONField;

public class ResultData {
    @JSONField
    public int code;
    @JSONField
    public String msg;
    @JSONField
    public String data;
    @JSONField
    public void setCode(int code) {
        this.code = code;
    }
    @JSONField
    public int getCode() {
        return code;
    }
    @JSONField
    public String getData() {
        return data;
    }
    @JSONField
    public void setData(String data) {
        this.data = data;
    }
    @JSONField
    public void setMsg(String msg) {
        this.msg = msg;
    }
    @JSONField
    public String getMsg(){
        return  msg;
    }
}
