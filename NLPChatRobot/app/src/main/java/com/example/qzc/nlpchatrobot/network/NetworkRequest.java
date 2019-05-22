package com.example.qzc.nlpchatrobot.network;

import android.graphics.Bitmap;
import java.util.Map;

public interface NetworkRequest {
    String doPost(String urlPath, Map<String, String> paramsMap);
    String uploadFile(Bitmap file, String RequestURL, Map<String, String> param, String imageName);
}
