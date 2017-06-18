package com.example.tomoki.myheartrate;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.cardemulation.HostNfcFService;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.tomoki.myheartrate.R.id.textView;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getName();
    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK"};
    private TextView mTextView;
    private Handler handler;
    private Button start,end;

////    wearableに情報を送信する関連
//    private Button sendmsg;
//    private GoogleApiClient mGoogleApiClient;
//    private String mNode;


//    音声認識関連
    TextView resultText;
    Button buttonTest;
    private int lang;
    private static final int REQUEST_CODE = 1000;

//    clientに送信する関連
    private GoogleApiClient mGoogleApiClient;
    public String mNode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


//        音声認識実験

//        // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
//        lang = 0;
//        // 認識結果を表示させる
//        resultText = (TextView)findViewById(R.id.result_voice_test);
//
//        buttonTest = (Button)findViewById(R.id.voice_test);
//        buttonTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 音声認識を開始
//                speech();
//            }
//        });


        start=(Button)findViewById(R.id.start_service);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, MyService.class));

//                追加部分
                TextView txt = (TextView) findViewById(R.id.view_heatbeat);
                txt.setText("hello");
//                txt.setText(MyService.msg);
            }
        } );

        end=(Button)findViewById(R.id.end_service);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        } );

////        wearableに送信テスト
//        sendmsg=(Button)findViewById(R.id.rtomita_test);
//        sendmsg.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                Log.d("送信テスト","送信準備はできたよ");
//
//                Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, "test", null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
//                    @Override
//                    public void onResult(MessageApi.SendMessageResult result) {
//                        if (!result.getStatus().isSuccess()) {
//                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
//                        }
//                    }
//                });
//            }
//        });

        mTextView = (TextView) findViewById(R.id.text);
        Log.d(TAG, "start");
    }

//      音声認識
    public void speech(){
        // 音声認識が使えるか確認する
        try {
            // 音声認識の　Intent インスタンス
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            if(lang == 0){
                // 日本語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.JAPAN.toString() );
            }
            else if(lang == 1){
                // 英語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString() );
            }
            else if(lang == 2){
                // Off line mode
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            }
            else{
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            }

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力");
            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e) {
            resultText.setText("No Activity " );
        }

    }

    // 結果を受け取るために onActivityResult を設置
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // 認識結果を ArrayList で取得
            ArrayList<String> candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(candidates.size() > 0) {
                // 認識結果候補で一番有力なものを表示
                resultText.setText( candidates.get(0));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
