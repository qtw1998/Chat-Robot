package com.example.qzc.nlpchatrobot.databaseManagement;

public class Msg {

    //define the different robot types
    public static final int ROBOT_1 = 11;
    public static final int ROBOT_2 = 12;

    public static final int TYPE_RECEIVED = 101;
    public static final int TYPE_SENT = 102;
    public static final int TYPE_PHOTO = 103;

    private String content;
    private int type;
    private int id;
    private int robotType;

    public Msg(String content, int type, int id, int robotType){
        this.content = content;
        this.type = type;
        this.id = id;
        this.robotType = robotType;
    }

    public String getContent(){
        return content;
    }

    public int getType(){
        return type;
    }

    public int getId() { return id; }

    public int getRobotType() {return robotType; }
}
