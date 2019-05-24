package com.example.qzc.nlpchatrobot.voice_process;
import android.content.Context;
import android.widget.EditText;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;



public class IatProcessVoiceToText implements VoiceToText {

    //这个类用于实现语音输入转文字的功能，类中部分方法直接对图形化界面操作，
    //如调出录音窗口，将录好的语音转为文字并显示到EditText中。
    //这个构件应属于一个用户界面的构件。

    private String voiceToTextResult = "";
    private RecognizerDialog mIatDialog;
    private Context mContext;
    private EditText inputEditText;



    //Context可以理解为上下文环境，即当前的Activity；EditText为显示语音转为文字的图形化控件
    public IatProcessVoiceToText(Context context, EditText editText){
        mContext = context;
        inputEditText = editText;
    }


    @Override
    public void initListenerDialog() {
        //科大讯飞初始化
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID +"=5cb97b62");
        //初始化语音监听器
        RecognizerDialogListener mRListener = new RecognizerDialogListener()  {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                //这个函数在一次语音识别中会被多次调用
                //处理返回的听写结果，并在UI中显示
                String text = parseIatResult(results.getResultString());
                voiceToTextResult += text;
                inputEditText.setText(voiceToTextResult);
                //如果是一次听写中最后一次返回，则将本次所有内容清除
                if(isLast){voiceToTextResult="";}
            }
            @Override
            public void onError(SpeechError speechError) {}
        };
        //初始化语音输入窗口并将语音监听器进行对应绑定
        mIatDialog = new RecognizerDialog(mContext, null);
        mIatDialog.setListener(mRListener);
        //设置语音输入窗口的相关属性
        setIatParam(mIatDialog);
    }

    @Override
    public void startListeningAndShowResult() {
        mIatDialog.show();
    }


    private void setIatParam(RecognizerDialog iatDialog) {
        //这个函数用于设置语音输入窗口的各个参数

        // 清空参数
        // mIatDialog.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        iatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        // 设置返回结果格式
        iatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

        // 设置语言区域
        iatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        iatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        iatDialog.setParameter(SpeechConstant.VAD_EOS, "2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        iatDialog.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        iatDialog.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        iatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, mContext.getExternalCacheDir().getPath() + "/cache_audio.wav");
    }


    private static String parseIatResult(String json) {
        //这个函数将转换后得到的json格式转为String
        StringBuilder ret = new StringBuilder();
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
}
