package com.example.qzc.nlpchatrobot.databaseManagement;

import org.litepal.LitePal;
import java.util.List;


public class DatabaseManagement implements UserInfoManagement, ChatRecordManagement {

    @Override
    public void saveUserInfo(String key, String info) {
        //update or save the user info (icon, picture path etc.) into the local database
        List<UserInfo> userInfos = LitePal.where("key = ?", key).find(UserInfo.class);
        if (userInfos.isEmpty()){
            UserInfo userInfo = new UserInfo();
            userInfo.setKey(key);
            userInfo.setInfo(info);
            userInfo.save();
        }
        else{
            UserInfo userinfo = userInfos.get(0);
            userinfo.setInfo(info);
            userinfo.save();
        }
    }

    @Override
    public String readUserInfo(String key) {
        List<UserInfo> userInfos = LitePal.where("key = ?", key).find(UserInfo.class);
        if (userInfos.isEmpty()){
            return null; }
        else {
            String userInfo = userInfos.get(0).getInfo();
            return userInfo;}
        }

    @Override
    public int readLatestChatRecordID() {
        //find the latest record id in the local database
        ChatRecord latestRecord = LitePal.findLast(ChatRecord.class);
        if (latestRecord == null){
            return 1;
        }
        else{
            return latestRecord.getId() + 1;
        }
    }

    @Override
    public void saveChatRecords(Msg msg) {
        //save the chat records into the local database
        ChatRecord record = new ChatRecord();
        record.setId(msg.getId());
        record.setMessage(msg.getContent());
        record.setType(msg.getType());
        record.setRobotType(msg.getRobotType());
        record.save();
    }


}

