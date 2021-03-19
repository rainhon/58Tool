package com.github.rainhon.responseData;

import com.alibaba.fastjson.annotation.JSONField;

public class PhoneData {
    @JSONField
    public String phone;
    @JSONField
    public int phoneId;

    public String getPhone() {
        return phone;
    }

    public int getPhoneId() {
        return phoneId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPhoneId(int phoneId) {
        this.phoneId = phoneId;
    }
}
