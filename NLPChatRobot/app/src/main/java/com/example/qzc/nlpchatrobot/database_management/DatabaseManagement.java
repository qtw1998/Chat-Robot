package com.example.qzc.nlpchatrobot.database_management;


import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManagement implements UserInfoManagementInterface, ChatRecordManagementInterface {

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


    @Override
    public List<Msg> readChatRecords(int lastToLoadMsgId) {
        //当recyclerView中有部分消息时，用此函数返回消息记录
        try{
            if (lastToLoadMsgId <= 0){
                //本地数据库中没有任何消息记录
                return null;
            }
            else{
                List<Msg> msgList = loadChatRecords(lastToLoadMsgId, 10);
                return msgList;
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    @Override
    public List<Msg> readChatRecords() {
        //当recyclerView中没有任何消息时，用此函数返回消息记录
        try{
            ChatRecord lastChatRecord = LitePal.findLast(ChatRecord.class);
            if (null == lastChatRecord){
                //本地数据库中没有任何消息记录
                return null;
            }
            else{
                int lastToLoadMsgId = lastChatRecord.getId();
                List<Msg> msgList = loadChatRecords(lastToLoadMsgId, 10);
                return msgList;
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    private List<Msg> loadChatRecords(int lastToLoadMsgId, int loadSize) {
        //从数据库中读取一定长度的历史消息记录
        try {
            List<Msg> msgList = new ArrayList<Msg>();
            for (int id=lastToLoadMsgId; id>(lastToLoadMsgId-loadSize) && id>0; id--){
                ChatRecord chatRecord = LitePal.find(ChatRecord.class, id);
                Msg recordMsg = new Msg(chatRecord.getMessage(), chatRecord.getType(), chatRecord.getId(), chatRecord.getRobotType());
                msgList.add(0, recordMsg);
            }
            return msgList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

