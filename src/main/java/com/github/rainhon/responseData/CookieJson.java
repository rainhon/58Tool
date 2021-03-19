package com.github.rainhon.responseData;

import com.alibaba.fastjson.annotation.JSONField;

public class CookieJson {
//                        .domain(o.getString("domain"))
//            .expiresOn(o.getString("expiry"))
//            .isHttpOnly(Boolean.getBoolean(o.getString("httpOnly")))
//            .isSecure(Boolean.getBoolean(o.getString("secure")))
//            .path(o.getString("path"))
    @JSONField
    String name;
    @JSONField
    String value;
    @JSONField
    String domain;
    @JSONField
    Long expiry;
    @JSONField
    String httpOnly;
    @JSONField
    String secure;
    @JSONField
    String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public String getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(String httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
