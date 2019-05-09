package com.example.qzc.nlpchatrobot;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import site.gemus.openingstartanimation.OpeningStartAnimation;




public class ChatActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {


    private Button sendButton;
    private EditText inputEditText;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Msg> msgList = new ArrayList<>();
    private int latestRecordId;
    private int robotType = Msg.ROBOT_1;
    private DrawerLayout mDrawerLayout;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private Uri imageUri;
    public static final int REQUEST_TAKE_PHOTO = 1001;
    public static final int REQUEST_SELECT_SENT_PHOTO = 1002;
    public static final int REQUEST_SELECT_BACKGROUND_PHOTO = 1003;
    public static final int REQUEST_ASK_PERMISSIONS = 2001;
    public static final String KEY_CHAT_BACKGROUND = "keyChatBackground";




    private SpeechRecognizer mIat;
    private RecognizerDialog mIatDialog;
    private RecognizerDialogListener mRListener;

    private ImageButton microButton;
    private TextView tv;
    private String voiceToTextResult;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //connect to the local database
        Connector.getDatabase();
        //read the latest chat record message ID
        readLatestRecordId();

        // set the network status receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

        initView();

        getPermissions();




        SpeechUtility.createUtility(ChatActivity.this, SpeechConstant.APPID +"=5ccc52cc");

        mRListener = new RecognizerDialogListener() {

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                String text = parseIatResult(results.getResultString());
                voiceToTextResult += text;
                tv.setText(voiceToTextResult);
                if (isLast) {
                    voiceToTextResult = "";
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        };

        mIatDialog = new RecognizerDialog(ChatActivity.this, null);
        mIatDialog.setListener(mRListener);

        tv = (TextView)findViewById(R.id.input_edit_text);
        microButton = (ImageButton) findViewById(R.id.microButton);
        microButton.setOnClickListener(this);




    }

    private void setIatParam(String filename) {
        // 清空参数
        mIatDialog.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        // 设置返回结果格式
        mIatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIatDialog.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIatDialog.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, getExternalCacheDir().getPath() + filename + ".wav");
    }

    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }


    private void initView(){


        //add by able for soft keyboard show
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //end by able to avoid the distortion of background image
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


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

        //swipeRefreshLayout is used to load the chat records
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);

        //navigationView is the drawer layout starting from the left side of the screen
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        //load the chat background
        List<UserInfo> userBackground = LitePal.where("key = ?", KEY_CHAT_BACKGROUND).find(UserInfo.class);
        if (!userBackground.isEmpty()){ setUserChatBackground(userBackground.get(0).getInfo()); }



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private void getPermissions(){
        //get the permissions
        int hasCameraPermission = checkSelfPermission("android.permission.CAMERA");
        int hasWriteExternalStoragePermission = checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED ||
                hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED ){
            requestPermissions(new String[] {"android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_ASK_PERMISSIONS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_ASK_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED){
                    // Permission Denied
                    Toast.makeText(ChatActivity.this, "Permissions denied.\nPlease check the permission manually." +
                            "\nOR it cannot work properly.", Toast.LENGTH_LONG).show();
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
                    updateChatView(msg);
                    inputEditText.setText("");
                    //codes about using neutral network API

                    autoRepeater(msg.getContent());
                }
                break;
            case R.id.microButton:
                Toast.makeText(ChatActivity.this, "clicked voice input", Toast.LENGTH_SHORT).show();
                setIatParam("cache_audio");
                mIatDialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        readChatRecords();
        swipeRefreshLayout.setRefreshing(false);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // for the items in the navigation view
        switch (menuItem.getItemId()){
            case R.id.robot_1:
                Toast.makeText(ChatActivity.this, "Switch to Robot 1", Toast.LENGTH_SHORT).show();
                robotType = Msg.ROBOT_1;
                //codes about using neutral network API 1


                mDrawerLayout.closeDrawers();
                break;
            case R.id.robot_2:
                Toast.makeText(ChatActivity.this, "Switch to Robot 2", Toast.LENGTH_SHORT).show();
                robotType = Msg.ROBOT_2;
                //codes about using neutral network API 2


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
                    saveUserInfo(KEY_CHAT_BACKGROUND, imgPath);
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
            updateChatView(msg);
            autoRepeater(msg.getContent());
            //crop and compress the image if needed, then send it
            processChatImage(imagePath);

        }
        else{
            Toast.makeText(ChatActivity.this,"Fail to load the image.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ChatActivity.this,"Failed to load the image.", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateChatView(Msg msg){
        // update and save the chat record
        msgList.add(msg);
        //show the latest sent message
        adapter.notifyItemChanged(msgList.size()-1);
        //scroll to the latest sent message
        msgRecyclerView.scrollToPosition(msgList.size()-1);
        saveChatRecords(msg);

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
                saveChatRecords(msg);
            }
        }, 1000);
    }


    private void saveChatRecords(Msg msg){
        //save the chat records into the local database
        ChatRecord record = new ChatRecord();
        record.setId(msg.getId());
        record.setMessage(msg.getContent());
        record.setType(msg.getType());
        record.setRobotType(msg.getRobotType());
        record.save();
        latestRecordId = latestRecordId + 1;
    }

    private void saveUserInfo(String key, String info){
        //update or save the user info (icon, picture etc.) into the local database
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


    private void readLatestRecordId(){
        //find the latest record id in the local database
        ChatRecord latestRecord = LitePal.findLast(ChatRecord.class);
        if (latestRecord == null){
            latestRecordId = 1;
        }
        else{
            latestRecordId = latestRecord.getId() + 1;
        }
    }

    private void readChatRecords(){
        //Load chat records from the local database
        try{
            int latestToLoadMsgId = 0;
            if (msgList.isEmpty()) {
                ChatRecord lastChatRecord = LitePal.findLast(ChatRecord.class);
                if (null == lastChatRecord){
                    Toast.makeText(this, "No More Chat Records", Toast.LENGTH_SHORT).show();
                }
                else{ latestToLoadMsgId = lastChatRecord.getId(); }
            }
            else{ latestToLoadMsgId = msgList.get(0).getId() - 1; }


            if (latestToLoadMsgId <= 0){
                Toast.makeText(this, "No More Chat Records", Toast.LENGTH_SHORT).show();
            }
            else{
                // load 10 pieces of messages at one time
                for (int id=latestToLoadMsgId; id>(latestToLoadMsgId-10) && id>0; id--){
                    ChatRecord chatRecord = LitePal.find(ChatRecord.class, id);
                    Msg recordMsg = new Msg(chatRecord.getMessage(), chatRecord.getType(), chatRecord.getId(), chatRecord.getRobotType());
                    msgList.add(0, recordMsg);
                    adapter.notifyItemInserted(0);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
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

            //socket communication for test use
            //manually set the Current Ip and the available port
            //String host = "192.168.43.104";
            //String host = "10.166.214.4";
            //final int port = 5050;

            //Socket socket;
            //try { //建立连接
            //    socket = new Socket(host, port);
            //    //获取输出流，通过这个流发送消息
            //    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            //    socketSendBitmapImage(out, croppedBitmap);
            //    out.close();
            //    socket.close();
            //}
            //    catch (IOException e) { e.printStackTrace(); return false;}





            return true;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            //work in UI thread
            progressDialog.setMessage("Processing ......");
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //work in UI thread
            progressDialog.dismiss();
            if (result){
                Toast.makeText(ChatActivity.this, "Process succeeded", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ChatActivity.this,"Process failed", Toast.LENGTH_SHORT).show();
            }
        }

        // this method should not be used in UI thread; use it in the image process task only
        private void socketSendBitmapImage(DataOutputStream outputStream, Bitmap bitmap) throws IOException{
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bout);

            long len = bout.size();

            Log.i("sendImgMsg", "len: "+len);

            outputStream.write(bout.toByteArray());
            outputStream.flush();
        }

        // this method should not be used in UI thread; use it in the image process task only
        private String socketReceiveImageCaption() {
            Socket socket;
            String host = "192.168.43.104";
            //String host = "10.166.214.4";
            final int port = 5050;
            try{
                socket = new Socket(host, port);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                long len = inputStream.readLong();
                byte[] bytes = new byte[(int)len];
                inputStream.read(bytes);
                String caption = new String(bytes);
                return caption;

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }






    }

    private Bitmap cropBitmapImage(double ratio, Bitmap originalBitmap){

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

