package com.example.qzc.nlpchatrobot;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class UserInfo extends LitePalSupport {
    @Column(unique = true, nullable = false)
    private String key;

    private String info;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
