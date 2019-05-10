package com.example.qzc.nlpchatrobot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author Jun
 * @time 2016/9/1  18:40
 * @desc 网络请求
 */
public class NetWorkUtils {

    /**
     * GET的网络请求
     *
     * @param urlPath
     * @return
     */
    public static String testDoGet(String urlPath) {

        String result = "";
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        Log.i("amumu", "asdsa");

        try {
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Log.i("amumu", "a11");
            int code = conn.getResponseCode();
            Log.i("amumu", "a1122");
            if (code == 200) {
                Log.i("amumu", "a11333");
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
            }
            Log.i("amumu", "a14441");
        } catch (Exception e) {
            Log.i("amumu", "444");
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

    /**
     * POST的网络请求
     *
     * @param urlPath
     * @param paramsMap
     * @return
     */
    public static String doPost(String urlPath, Map<String, String> paramsMap) {
        String result = "";
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-bocang-Authorization", "token"); //token可以是用户登录后的token等等......
            conn.setDoOutput(true);
            String parames = "";
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                parames += ("&" + entry.getKey() + "=" + entry.getValue());
            }
            conn.getOutputStream().write(parames.substring(1).getBytes());
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

    private static int TIME_OUT = 10 * 1000;   //超时时间
    private static String CHARSET = "utf-8"; //设置编码

    /**
     * android上传文件到服务器
     *
     * @param file       需要上传的文件
     * @param RequestURL 请求的rul
     * @return 返回响应的内容
     */
    public static String uploadFile(Drawable file, String RequestURL, Map<String, String> param, String imageName) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        // 显示进度框
        //      showProgressDialog();
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
            conn.setRequestProperty("X-bocang-Authorization", "token"); //token可以是用户登录后的token等等......
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {
                Log.v("520it", "触发到");
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();

                String params = "";
                if (param != null && param.size() > 0) {
                    Iterator<String> it = param.keySet().iterator();
                    while (it.hasNext()) {
                        sb = null;
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
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的   比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"")
                        .append("avatar")
                        .append("\"")
                        .append(";filename=\"")
                        .append(imageName)
                        .append("\"\n");
                sb.append("Content-Type: image/png");
                sb.append(LINE_END).append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = Drawable2InputStream(file);
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
                /**
                 * 获取响应码  200=成功
                 * 当响应成功，获取响应的流
                 */

                int res = conn.getResponseCode();
                System.out.println("res=========" + res);
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                } else {
                }
            } else {
                Log.v("520it", "触发不到");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * android上传文件到服务器
     *
     * @param RequestURL 请求的rul
     * @return 返回响应的内容
     */
    public static String uploadMoreFile(List<Bitmap> files, String RequestURL, Map<String, String> param, String imageName) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        // 显示进度框
        //      showProgressDialog();
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
            conn.setRequestProperty("X-bocang-Authorization", "token"); //token可以是用户登录后的token等等......
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);


            Log.v("520it", "触发到");
            /**
             * 当文件不为空，把文件包装并且上传
             */
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            StringBuffer sb = new StringBuffer();

            String params = "";
            if (param != null && param.size() > 0) {
                Iterator<String> it = param.keySet().iterator();
                while (it.hasNext()) {
                    sb = null;
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

            /**
             * 这里重点注意：
             * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名的   比如:abc.png
             */

            for (int i = 0; i < files.size(); i++) {
                sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"")
                        .append("image[]")
                        .append("\"")
                        .append(";filename=\"")
                        .append(System.currentTimeMillis()+".jpg")
                        .append("\"\n");
                sb.append("Content-Type: image/png");
                sb.append(LINE_END).append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = Bitmap2InputStream(files.get(i));
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();

                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
            }


            dos.flush();
            /**
             * 获取响应码  200=成功
             * 当响应成功，获取响应的流
             */

            int res = conn.getResponseCode();
            System.out.println("res=========" + res);
            if (res == 200) {
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                result = sb1.toString();
            } else {
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * android上传文件到服务器
     *
     * @param file       需要上传的文件
     * @param RequestURL 请求的rul
     * @return 返回响应的内容
     */
    public static String uploadFile(Bitmap file, String RequestURL, Map<String, String> param, String imageName) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        // 显示进度框
        //      showProgressDialog();
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
            conn.setRequestProperty("X-bocang-Authorization", "token"); //token可以是用户登录后的token等等......
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();

                String params = "";
                if (param != null && param.size() > 0) {
                    Iterator<String> it = param.keySet().iterator();
                    while (it.hasNext()) {
                        sb = null;
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
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的   比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"")
                        .append("image")
                        .append("\"")
                        .append(";filename=\"")
                        .append(imageName)
                        .append("\"\n");
                sb.append("Content-Type: image/png");
                sb.append(LINE_END).append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = Bitmap2InputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }


                is.close();
                //                dos.write(file);
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);

                dos.flush();
                /**
                 * 获取响应码  200=成功
                 * 当响应成功，获取响应的流
                 */

                int res = conn.getResponseCode();
                System.out.println("res=========" + res);
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                } else {
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSomething(String path) {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        String returnCode = "-1";// 默认-1表示提交失败
        try {
            URL url = new URL(path);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30000); // 连接的超时时间
            conn.setReadTimeout(30000); // 读数据的超时时间
            conn.setDoOutput(true); // 必须设置此方法, 允许输出
            conn.setRequestProperty("Content-Type", "application/text"); // 设置请求头消息,可以设置多个
            conn.connect();

            int responseCode = conn.getResponseCode();
            returnCode = responseCode + "";
            if (responseCode == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                returnCode = baos.toString(); // 把流中的数据转换成字符串, 采用的编码是: utf-8
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != baos)
                    baos.close();
                if (null != is)
                    is.close();
                if (null != conn)
                    conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnCode;
    }


    /**
     * GET的网络请求
     * @param urlPath
     * @return
     */
    public static String doGet(String urlPath){
        String result="";
        BufferedReader reader=null;
        HttpURLConnection conn=null;
        try{
            URL url=new URL(urlPath);
            conn= (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30000); // 连接的超时时间
            conn.setReadTimeout(30000); // 读数据的超时时间
            conn.setDoOutput(true); // 必须设置此方法, 允许输出
            conn.setRequestProperty("Content-Type", "application/text"); // 设置请求头消息,可以设置多个
            conn.connect();

            int code = conn.getResponseCode();
            if(code==200){
                reader=new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String line="";
                while((line=reader.readLine())!=null) {
                    result+=line;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(conn!=null){
                conn.disconnect();
            }
        }

        return result;
    }

    // 将Bitmap转换成InputStream
    public static InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 65, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    // Drawable转换成InputStream
    public static InputStream Drawable2InputStream(Drawable d) {
        Bitmap bitmap = drawable2Bitmap(d);
        return Bitmap2InputStream(bitmap);
    }

    // Drawable转换成Bitmap
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

