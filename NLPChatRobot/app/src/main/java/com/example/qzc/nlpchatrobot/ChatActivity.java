package com.example.qzc.nlpchatrobot;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

import site.gemus.openingstartanimation.OpeningStartAnimation;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private Button sendButton;
    private EditText inputEditText;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<>();
    private int latestRecordId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Connector.getDatabase();
        setLatestRecordId();
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setOpeningAnimation();


        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        inputEditText = (EditText) findViewById(R.id.input_edit_text);

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);

        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendButton:
                //Toast.makeText(ChatActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                final String content = inputEditText.getText().toString();
                if(!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    //show the latest sent message
                    adapter.notifyItemChanged(msgList.size()-1);
                    //scroll to the latest sent message
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                    inputEditText.setText("");

                    saveChatRecords(msg);

                    //delay 1 sec to repeat
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            autoRepeater(content);
                        }
                    }, 1000);

                }
                break;
            default:
                break;
        }
    }

    private void setOpeningAnimation(){
        // external animation dependency
        String appStatement = "Create it!";
        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setAppStatement(appStatement).create();
        openingStartAnimation.show(this);
    }

    private void autoRepeater(String content){
        // this is a test repeater.
        Msg msg = new Msg(content, Msg.TYPE_RECEIVED);
        msgList.add(msg);
        //show the latest sent message
        adapter.notifyItemChanged(msgList.size()-1);
        //scroll to the latest sent message
        msgRecyclerView.scrollToPosition(msgList.size()-1);
        saveChatRecords(msg);

    }

    private void saveChatRecords(Msg msg){
        ChatRecord record = new ChatRecord();
        latestRecordId = latestRecordId + 1;
        record.setId(latestRecordId);
        record.setMessage(msg.getContent());
        record.setType(msg.getType());
        record.save();

    }

    private void setLatestRecordId(){
        ChatRecord latestRecord = LitePal.findLast(ChatRecord.class);
        if (latestRecord == null){
            latestRecordId = 0;
        }
        else{
            latestRecordId = latestRecord.getId();
        }
    }
}
