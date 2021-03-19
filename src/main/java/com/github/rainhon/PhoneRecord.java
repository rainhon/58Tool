package com.github.rainhon;

import com.github.rainhon.util.UtilCookie;
import org.openqa.selenium.Cookie;

import java.util.Set;

public class PhoneRecord {
    private String phone = null;
    private String status = null;
    private PhoneStatus phoneStatus = null;
    private int phoneId;
    private String account;
    private String password;
    private Set<Cookie> cookies;
    private String cookiesString = "";

    public PhoneRecord(){

    }

    public PhoneRecord(String phone, PhoneStatus phoneStatus, int phoneId){
        this.phone = phone;
        this.status = phoneStatus.getDescription();
        this.phoneStatus = phoneStatus;
        this.phoneId = phoneId;
    }

    public void setPhoneId(int phoneId) {
        this.phoneId = phoneId;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPhoneStatus(PhoneStatus phoneStatus) {
        this.phoneStatus = phoneStatus;
        this.status = phoneStatus.getDescription();
    }

    public String getPhone() {
        return phone;
    }
    public PhoneStatus getPhoneStatus() {
        return phoneStatus;
    }

    public String getStatus() {
        return status;
    }

    public int getPhoneId(){
        return this.phoneId;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(Set<Cookie> cookies) {
        this.cookiesString = UtilCookie.buildCookieString(cookies);
        this.cookies = cookies;
    }

    public String getCookiesString() {
        return cookiesString;
    }

    public void setCookiesString(Set<Cookie> cookies) {
        this.cookies = cookies;
        this.cookiesString = UtilCookie.buildCookieString(cookies);
    }

    enum PhoneStatus{
        FRESH("新号码"),
        INVALID("无法发送"),
        BLACK("已拉黑"),
        REGISTERED("已注册"),
        USEFUL("可用");
        String description;
        PhoneStatus(String description){
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
