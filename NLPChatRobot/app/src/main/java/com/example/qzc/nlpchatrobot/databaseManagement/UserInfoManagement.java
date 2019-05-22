package com.example.qzc.nlpchatrobot.databaseManagement;

public interface UserInfoManagement {
    void saveUserInfo(String key, String info);
    String readUserInfo(String key);
}
