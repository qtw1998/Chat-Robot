package com.example.qzc.nlpchatrobot;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import site.gemus.openingstartanimation.OpeningStartAnimation;

import static android.os.Environment.DIRECTORY_PICTURES;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    private Button sendButton;
    private EditText inputEditText;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<>();
    private int latestRecordId;
    private DrawerLayout mDrawerLayout;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private Uri imageUri;
    public static final int REQUEST_TAKE_PHOTO = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Connector.getDatabase();
        setLatestRecordId();

        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }

        //uncomment the following to show the opening animation
        //setOpeningAnimation();


        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        inputEditText = (EditText) findViewById(R.id.input_edit_text);

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);

        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // set the network status receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendButton:
                //Toast.makeText(ChatActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                String content = inputEditText.getText().toString();
                if(!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    //show the latest sent message
                    adapter.notifyItemChanged(msgList.size()-1);
                    //scroll to the latest sent message
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                    inputEditText.setText("");
                    saveChatRecords(msg);

                    //codes about using neutral network API

                    autoRepeater(msg.getContent());

                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.robot_1:
                Toast.makeText(ChatActivity.this, "Switch to Robot 1", Toast.LENGTH_SHORT).show();
                adapter.setRobotType(MsgAdapter.ROBOT_1);
                //codes about using neutral network API 1


                mDrawerLayout.closeDrawers();
                break;
            case R.id.robot_2:
                Toast.makeText(ChatActivity.this, "Switch to Robot 2", Toast.LENGTH_SHORT).show();
                adapter.setRobotType(MsgAdapter.ROBOT_2);
                //codes about using neutral network API 2


                mDrawerLayout.closeDrawers();
                break;
            case R.id.camera:
                openCamera();
                break;
            default:
        }
        return true;
    }


    private void openCamera(){
        // Create a File Object to store the image taken
        String imageName = String.format("output_image_%d.jpg", latestRecordId);
        File outputImage = new File(getExternalFilesDir("images"), imageName);
        try{
            if (outputImage.exists()){outputImage.delete();}
            outputImage.createNewFile();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT>=24){
            imageUri = FileProvider.getUriForFile(ChatActivity.this,
                    "com.example.qzc.nlpchatrobot.fileprovider", outputImage);
        }
        else {
            imageUri = Uri.fromFile(outputImage);
        }

        // open the camera
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    try{
                        //set the bitmap picture in the adapter
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        adapter.setRightImgBitmap(bitmap);

                        String imageName = String.format("output_image_%d.jpg", latestRecordId);
                        String imgSavePath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.qzc.nlpchatrobot/files/images/" + imageName;
                        Msg msg = new Msg(imgSavePath, Msg.TYPE_PHOTO);

                        // save the image chat record
                        //image save path: "/storage/emulated/0/Android/data/com.example.qzc.nlpchatrobot/files/images/"
                        msgList.add(msg);
                        //show the latest sent message
                        adapter.notifyItemChanged(msgList.size()-1);
                        //scroll to the latest sent message
                        msgRecyclerView.scrollToPosition(msgList.size()-1);
                        saveChatRecords(msg);

                        autoRepeater(msg.getContent());


                    }
                    catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setOpeningAnimation(){
        // Use the external animation dependency to show the opening animation
        String appStatement = "Create it!";
        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setAppStatement(appStatement).create();
        openingStartAnimation.show(this);
    }


    private void autoRepeater(final String content){
        //delay 1 sec to repeat
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // this is a test repeater.
                Msg msg = new Msg(content, Msg.TYPE_RECEIVED);
                msgList.add(msg);
                //show the latest sent message
                adapter.notifyItemChanged(msgList.size()-1);
                //scroll to the latest sent message
                msgRecyclerView.scrollToPosition(msgList.size()-1);
                saveChatRecords(msg);
            }
        }, 1000);
    }


    private void saveChatRecords(Msg msg){
        //save the chat records into the local database
        ChatRecord record = new ChatRecord();
        record.setId(latestRecordId);
        record.setMessage(msg.getContent());
        record.setType(msg.getType());
        record.save();
        latestRecordId = latestRecordId + 1;
    }


    private void setLatestRecordId(){
        //find the latest record id in the local database
        ChatRecord latestRecord = LitePal.findLast(ChatRecord.class);
        if (latestRecord == null){
            latestRecordId = 1;
        }
        else{
            latestRecordId = latestRecord.getId();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use to create the toolbar menu
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // use to listen to the item in the toolbar menu
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.contact_us:
                Toast.makeText(ChatActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                //codes about contacting to the author

                break;
            default:
        }
        return true;
    }
}
