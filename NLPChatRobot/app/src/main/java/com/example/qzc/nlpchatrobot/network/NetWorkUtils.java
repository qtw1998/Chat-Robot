package com.example.qzc.nlpchatrobot.network;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;



public class NetWorkUtils implements NetworkRequestInterface {

    private static int TIME_OUT = 60 * 1000;   //设置超时时间
    private static String CHARSET = "utf-8"; //设置编码


    @Override
    public String doPost(String urlPath, Map<String, String> paramsMap) {
        //提交一次POST请求，paramsMap为提交请求时的相关参数，result为返回的response
        String result = "";
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setDoOutput(true);
            String parames = "";
            //添加POST访问的参数
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                parames += ("&" + entry.getKey() + "=" + entry.getValue());
            }
            conn.getOutputStream().write(parames.substring(1).getBytes());
            //如果有response则接收
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }



    @Override
    public String uploadFile(Bitmap file, String RequestURL, Map<String, String> param, String imageName) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString();  //边界标识，随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        // 显示进度框
        // showProgressDialog();
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {

                //当文件不为空，把文件包装，以类似于表单的形式并且上传
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb;

                String params = "";
                if (param != null && param.size() > 0) {
                    Iterator<String> it = param.keySet().iterator();
                    while (it.hasNext()) {
                        sb = new StringBuffer();
                        String key = it.next();
                        String value = param.get(key);
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                        sb.append("Content-Disposition: form-data; name=\"")
                                .append(key)
                                .append("\"")
                                .append(LINE_END)
                                .append(LINE_END);
                        sb.append(value).append(LINE_END);
                        params = sb.toString();
                        dos.write(params.getBytes());
                    }
                }
                sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                // name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                // filename是文件的名字，包含后缀名的   比如:abc.png
                sb.append("Content-Disposition: form-data; name=\"")
                        .append("image")
                        .append("\"")
                        .append(";filename=\"")
                        .append(imageName)
                        .append("\"\n");
                sb.append(LINE_END).append(LINE_END);
                dos.write(sb.toString().getBytes());
                //将Bitmap图片转为二进制数组发送
                InputStream is = Bitmap2InputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }


                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                // 获取响应码  200=成功
                // 当响应成功，获取响应的流
                int res = conn.getResponseCode();
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                }
            }
        } catch (Exception e) {
            Log.d("imageProcess", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    // 将Bitmap转换成InputStream
    private static InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 65, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

}
