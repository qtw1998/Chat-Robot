package com.example.qzc.nlpchatrobot.database_management;

public interface UserInfoManagementInterface {
    void saveUserInfo(String key, String info);
    String readUserInfo(String key);
}
