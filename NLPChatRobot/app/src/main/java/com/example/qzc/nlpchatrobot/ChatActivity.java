package com.example.qzc.nlpchatrobot;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.qzc.nlpchatrobot.database_management.DatabaseManagement;
import com.example.qzc.nlpchatrobot.database_management.Msg;
import com.example.qzc.nlpchatrobot.network.NetWorkUtils;
import com.example.qzc.nlpchatrobot.network.NetworkBroadcastManagement;
import com.example.qzc.nlpchatrobot.voice_process.IatProcessVoiceToText;

import org.litepal.tablemanager.Connector;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import site.gemus.openingstartanimation.OpeningStartAnimation;




public class ChatActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {



    private EditText inputEditText;

    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Msg> msgList = new ArrayList<>();

    private int latestRecordId;
    private int robotType = Msg.ROBOT_1;
    private DrawerLayout mDrawerLayout;

    //这个类用于监听网络状态
    private NetworkBroadcastManagement networkBM = new NetworkBroadcastManagement(this);
    //这个类用于科大讯飞语音处理的调用及相关图形界面
    private IatProcessVoiceToText mIatProcessVoiceToText;
    //这个类用于发送http请求，上传文字，图片
    private NetWorkUtils netWorkUtils = new NetWorkUtils();
    //这个类用于写入、获取本地数据库中的信息
    private DatabaseManagement dbManage = new DatabaseManagement();

    public static final int REQUEST_TAKE_PHOTO = 1001;
    public static final int REQUEST_SELECT_SENT_PHOTO = 1002;
    public static final int REQUEST_SELECT_BACKGROUND_PHOTO = 1003;
    private static final int PERMISSION_REQUEST = 2000;
    public static final String KEY_CHAT_BACKGROUND = "keyChatBackground";


    private final String requestUrl = "http://192.168.43.104:5050";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //连接到本地数据库
        Connector.getDatabase();

        //读取数据库中最后一条消息记录的ID便于读取本地消息历史记录
        latestRecordId = dbManage.readLatestChatRecordID();

        //注册网络状态监听器
        networkBM.registerReceiver();

        //图形界面初始化
        initView();

        //获取用户权限
        getPermissions();

        //初始化科大讯飞语音转换窗口参数，但不显示
        mIatProcessVoiceToText = new IatProcessVoiceToText(this, inputEditText);
        mIatProcessVoiceToText.initListenerDialog();
    }


    private void initView(){
        //使用键盘时隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //使用键盘时，整体界面向上滑动，避免背景图片缩放
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //设置框架布局文件
        setContentView(R.layout.activity_chat);

        //通过使用ActionBar美化标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }

        //以下语句用于显示打开APP时的动画效果
        setOpeningAnimation();

        //获取左侧划出窗体的实例
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //发送消息的Button，绑定listener
        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);

        //调用科大讯飞语音输入窗口的Button,绑定listener
        ImageButton microButton = (ImageButton) findViewById(R.id.microButton);
        microButton.setOnClickListener(this);

        //发送消息的文字框
        inputEditText = (EditText) findViewById(R.id.input_edit_text);

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        //swipeRefreshLayout只包装了recyclerView便于监听它的滑动情况，下拉使返回历史消息记录
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);

        //navigationView是左侧划出的drawerLayout，包含menu和header部分
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView只能监听menu部分的点击事件
        navView.setNavigationItemSelectedListener(this);

        //从本地数据库中加载用户的聊天背景
        String userBackgroundPath = dbManage.readUserInfo(KEY_CHAT_BACKGROUND);
        setUserChatBackground(userBackgroundPath);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销网络状态监听器
        networkBM.unregisterReceiver();
    }


    private void getPermissions() {
        //获取用户权限，弹出对话框
        final String[] permissions = {"android.permission.RECORD_AUDIO", "android.permission.READ_PHONE_STATE",
                "android.permission.WRITE_EXTERNAL_STORAGE",  "android.permission.CAMERA"};
        boolean requestOrNot = false;
        for (String permission: permissions) {
            int hasPermission = checkSelfPermission(permission);
            if(hasPermission != PackageManager.PERMISSION_GRANTED){
                requestOrNot = true;
            }
        }
        if(requestOrNot){
            requestPermissions(permissions, PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //根据获取权限的request值，进行相应操作
        switch (requestCode){
            case PERMISSION_REQUEST:
                boolean showWarnInfo = false;
                for (int result:grantResults) {
                    if(result != PackageManager.PERMISSION_GRANTED){showWarnInfo = true;}
                }
                if (showWarnInfo){
                    //用户拒绝授予权限，提示一下
                    Toast.makeText(this, "Required Permissions Denied.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendButton:
                String content = inputEditText.getText().toString();
                if(!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT, latestRecordId, robotType);
                    updateOneChatMsg(msg);
                    inputEditText.setText("");
                    //调用神经网络
                    autoRepeater(msg.getContent());
                }
                break;
            case R.id.microButton:
                //科大讯飞语音转换
                mIatProcessVoiceToText.startListeningAndShowResult();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        //readChatRecords();
        notifyChatRecordsUpdate();
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //navigationView中的menu里的item被选中时
        switch (menuItem.getItemId()){
            case R.id.robot_1:
                Toast.makeText(this, "Switch to Robot 1", Toast.LENGTH_SHORT).show();
                robotType = Msg.ROBOT_1;
                mDrawerLayout.closeDrawers();
                break;
            case R.id.robot_2:
                Toast.makeText(this, "Switch to Robot 2", Toast.LENGTH_SHORT).show();
                robotType = Msg.ROBOT_2;
                mDrawerLayout.closeDrawers();
                break;
            case R.id.camera:
                openCamera();
                break;
            case R.id.gallery:
                openAlbum(REQUEST_SELECT_SENT_PHOTO);
                break;
            case R.id.chat_background:
                openAlbum(REQUEST_SELECT_BACKGROUND_PHOTO);
                break;
            default:
        }
        return true;
    }


    private void openCamera(){
        // Create a File Object to store the image taken
        Uri imageUri;
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


    private void openAlbum(final int request_code){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, request_code);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case REQUEST_TAKE_PHOTO:
                String imageName = String.format("output_image_%d.jpg", latestRecordId);
                if (resultCode == RESULT_OK){
                    try{
                        //image save path: "/storage/emulated/0/Android/data/com.example.qzc.nlpchatrobot/files/images/"
                        String imgSavePath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.qzc.nlpchatrobot/files/images/" + imageName;
                        sendChatImage(imgSavePath);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    File emptyImage = new File(getExternalFilesDir("images"), imageName);
                    if (emptyImage.exists()){emptyImage.delete();}
                    }
                break;
            case REQUEST_SELECT_SENT_PHOTO:
                if (resultCode == RESULT_OK){
                    String imgPath = handleImageGetPathOnKitKat(data);
                    sendChatImage(imgPath);
                }
                break;
            case REQUEST_SELECT_BACKGROUND_PHOTO:
                if (resultCode == RESULT_OK){
                    String imgPath = handleImageGetPathOnKitKat(data);
                    setUserChatBackground(imgPath);
                    //从数据库中获取用户聊天背景图片地址
                    dbManage.saveUserInfo(KEY_CHAT_BACKGROUND, imgPath);
                }
                break;
            default:
                break;
        }
    }


    private String handleImageGetPathOnKitKat(Intent data){
        // for Android 4.4 and higher
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return imagePath;
    }


    private String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    private void sendChatImage(String imagePath){
        if (imagePath != null){
            Msg msg = new Msg(imagePath, Msg.TYPE_PHOTO, latestRecordId, robotType);
            // update and save the image chat record
            updateOneChatMsg(msg);
            autoRepeater(msg.getContent());
            //crop and compress the image if needed, then send it
            processChatImage(imagePath);
        }
        else{
            Toast.makeText(this,"Fail to load the image.", Toast.LENGTH_SHORT).show();
        }

    }


    private void processChatImage(String originalImagePath){
        //compress and crop the image in another thread
        //params    strings[0]:original image path  strings[1]:crop ratio 0-1   strings[2]:compress quality 1-100
        String quality = "100";
        String ratio = "1";
        new ImageProcessTask().execute(originalImagePath, ratio, quality);
    }


    private void setUserChatBackground(String imagePath){
        if (imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Drawable drawable = new BitmapDrawable(bitmap);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chat_activity_layout);
            linearLayout.setBackground(drawable);
        }
        else{
            Toast.makeText(this,"Failed to load the image.\nImage may be deleted.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateOneChatMsg(Msg msg){
        // update and save the chat record
        msgList.add(msg);
        //show the latest sent message
        adapter.notifyItemChanged(msgList.size()-1);
        //scroll to the latest sent message
        msgRecyclerView.scrollToPosition(msgList.size()-1);
        dbManage.saveChatRecords(msg);
        latestRecordId = latestRecordId + 1;

    }


    private void autoRepeater(final String content){
        //delay 1 sec to repeat
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // this is a test repeater.
                Msg msg = new Msg(content, Msg.TYPE_RECEIVED, latestRecordId, robotType);
                msgList.add(msg);
                //show the latest sent message
                adapter.notifyItemChanged(msgList.size()-1);
                //scroll to the latest sent message
                msgRecyclerView.scrollToPosition(msgList.size()-1);
                dbManage.saveChatRecords(msg);
                latestRecordId = latestRecordId + 1;
            }
        }, 1000);
    }



    private void notifyChatRecordsUpdate(){
        //将从数据库中读取的消息记录列表加载到recyclerView的最前端，实现加载历史记录的功能
        List<Msg> msgs;
        if(msgList.isEmpty()){
            msgs = dbManage.readChatRecords();
        }
        else {
            msgs = dbManage.readChatRecords(msgList.get(0).getId()-1);
        }
        if(msgs == null){
            Toast.makeText(this, "No More Chat Records", Toast.LENGTH_SHORT).show();
        }
        else {
            msgList.addAll(0, msgs);
            adapter.notifyItemRangeInserted(0, msgs.size());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 用于创建 toolbar menu
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 用于监听 toolbar menu中的items是否被选中
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.contact_us:
                Toast.makeText(this, "Contact Us", Toast.LENGTH_SHORT).show();
                //codes about contacting to the author

                break;
            default:
        }
        return true;
    }


    private void setOpeningAnimation(){
        // Use the external animation dependency to show the opening animation
        String appStatement = "Create it!";
        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setAppStatement(appStatement).create();
        openingStartAnimation.show(this);
    }


    // this class is used to process the real image fed to the neutral network
    private class ImageProcessTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        private String imageCaption;

        @Override
        protected void onPreExecute() {
            // work in UI thread
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            //work in a new thread
            //params    strings[0]:original image path  strings[1]:crop ratio   strings[2]:compress quality

            String originalImagePath = strings[0];
            Bitmap originalBitmap = BitmapFactory.decodeFile(originalImagePath);

            // the following path is used to store the image saved after compressed and cropped
            String imgSavePath = getExternalCacheDir().getPath() + "/cache_image.jpg";
            File imageFile = new File(imgSavePath);

            //center_crop the image with a ratio
            double ratio = Double.parseDouble(strings[1]);
            Bitmap croppedBitmap = cropBitmapImage(ratio, originalBitmap);

            //compress the image with a quality 1-100 to decrease the storage space
            int quality = Integer.parseInt(strings[2]);
            try{
                //对图片压缩同时写入文件流
                if (imageFile.exists()) {imageFile.delete();}
                imageFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

            //write image string (base 64 encoding) into the text file
            //String imageBase64Str = getImageBase64Str(croppedBitmap);

            try{
                imageCaption = netWorkUtils.uploadFile(croppedBitmap, requestUrl,null, "firstImage.jpg");
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            //work in UI thread
            progressDialog.setMessage("On processing");
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //work in UI thread
            progressDialog.dismiss();
            if (result){
                Toast.makeText(ChatActivity.this, "Process succeeded", Toast.LENGTH_SHORT).show();
                autoRepeater(imageCaption);
            }
            else{
                Toast.makeText(ChatActivity.this,"Process failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private Bitmap cropBitmapImage(double ratio, Bitmap originalBitmap){
        //根据比例中心裁剪Bitmap格式的图片
        Bitmap croppedBitmap;
        if (ratio > 0 && ratio < 1){
            int croppedWidth = (int) (ratio*originalBitmap.getWidth());
            int croppedHeight = (int) (ratio*originalBitmap.getHeight());
            int croppedXStart = (int) ((1 - ratio) * originalBitmap.getWidth()/2);
            int croppedYStart = (int) ((1 - ratio) * originalBitmap.getHeight()/2);
            croppedBitmap = Bitmap.createBitmap(originalBitmap, croppedXStart, croppedYStart, croppedWidth, croppedHeight);
        }
        else{
            croppedBitmap = Bitmap.createBitmap(originalBitmap);
        }
        return croppedBitmap;
    }

    private String getImageBase64Str(Bitmap bitmap){
        //将Bitmap格式的图片转为Base64编码字符串返回，并将字符串临时储存于一个文本文件，本函数暂未用到
        String imageBase64Str;
        try{
            ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteAOS);
            byte[] imageByteArray = byteAOS.toByteArray();

            //image text file (base 64 encoding)
            String imageTextFilePath = getExternalCacheDir().getPath() + "/cache_image.txt";
            File imageTextFile = new File(imageTextFilePath);

            if (imageTextFile.exists()) {imageTextFile.delete();}
            imageTextFile.createNewFile();

            FileOutputStream fileOS = new FileOutputStream(imageTextFile);

            imageBase64Str = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
            fileOS.write(imageBase64Str.getBytes());
            fileOS.close();
            return imageBase64Str;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


}

