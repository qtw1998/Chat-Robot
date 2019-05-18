package com.example.qzc.nlpchatrobot.iatvoice;
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

public class IatVoiceToText implements VoiceToText {
    private String voiceToTextResult = "";
    private RecognizerDialog mIatDialog;
    private RecognizerDialogListener mRListener;
    private Context mContext;
    private EditText inputEditText;


    public IatVoiceToText(Context context, EditText editText){
        mContext = context;
        inputEditText = editText;
    }

    @Override
    public void initParams() {
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID +"=5cb97b62");
        mRListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                String text = parseIatResult(results.getResultString());
                voiceToTextResult += text;

                showResult();
                if(isLast){voiceToTextResult="";}
            }
            @Override
            public void onError(SpeechError speechError) {}
        };
        mIatDialog = new RecognizerDialog(mContext, null);
        mIatDialog.setListener(mRListener);
        setIatParam();
    }

    @Override
    public void startListening() {
        mIatDialog.show();
    }

    @Override
    public void showResult() {
        inputEditText.setText(voiceToTextResult);
    }


    private void setIatParam() {
        // 清空参数
        // mIatDialog.setParameter(SpeechConstant.PARAMS, null);

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
        mIatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, mContext.getExternalCacheDir().getPath() + "/cache_audio.wav");
    }


    private static String parseIatResult(String json) {
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
