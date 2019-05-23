package com.example.qzc.nlpchatrobot.database_management;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class ChatRecord extends LitePalSupport {

    @Column(unique = true, nullable = false)
    private int id;

    private int type;

    private int robotType;

    private String message;

    public int getId(){
        return id;
    }

    public int getType(){
        return type;
    }

    public String getMessage(){
        return message;
    }

    public int getRobotType() {return robotType; }

    public void setId(int id){
        this.id = id;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setRobotType(int robotType) {this.robotType = robotType; }
}
