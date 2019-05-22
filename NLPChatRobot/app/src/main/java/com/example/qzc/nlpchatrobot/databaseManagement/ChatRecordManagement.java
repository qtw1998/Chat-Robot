package com.example.qzc.nlpchatrobot.databaseManagement;

public interface ChatRecordManagement {
    int readLatestChatRecordID();
    void saveChatRecords(Msg msg);
}
