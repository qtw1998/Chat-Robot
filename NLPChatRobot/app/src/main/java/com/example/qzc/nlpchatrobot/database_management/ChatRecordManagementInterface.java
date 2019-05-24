package com.example.qzc.nlpchatrobot.database_management;

import java.util.List;

public interface ChatRecordManagementInterface {
    int readLatestChatRecordID();
    void saveChatRecords(Msg msg);
    List<Msg> readChatRecords(int lastToLoadMsgId);
    List<Msg> readChatRecords();
}
