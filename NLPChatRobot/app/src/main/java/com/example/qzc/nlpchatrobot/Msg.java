package com.example.qzc.nlpchatrobot;

public class Msg {

    public static final int TYPE_RECEIVED = 101;
    public static final int TYPE_SENT = 102;
    public static final int TYPE_PHOTO = 103;
    private String content;
    private int type;
    private int id;

    public Msg(String content, int type, int id){
        this.content = content;
        this.type = type;
        this.id = id;
    }

    public String getContent(){
        return content;
    }

    public int getType(){
        return type;
    }

    public int getId() { return id; }
}
