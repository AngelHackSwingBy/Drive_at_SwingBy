package com.example.tomoki.myheartrate;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Measure;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static java.lang.Double.parseDouble;

/**
 * Created by Tomoki on 2017/05/30.
 */

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{
    private static final String TAG = MainActivity.class.getName();
    private String fileName="result.csv";
    SimpleDateFormat sdma,sdmb;
    private GoogleApiClient mGoogleApiClient;
    long startTimeMillis,nowTimeMillis;
    private String msg;
    private MySocket s;
    double time;

    //    心拍数の判断用flag
    Integer flag2 = 0;
    //    心拍数の閾値
    Double Threashold = 100.0;

    //    起動してからの時間
    int start_time = 5000;
    //    起動してからのフラグ
    int start_flag = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "MyService#onCreate", Toast.LENGTH_SHORT).show();
        sdma=new SimpleDateFormat("yyyy.MM.dd");
        sdmb=new SimpleDateFormat("HH:mm:ss:SSS");

        initFile();
        connect();

//        //        タイマー
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                start_flag = 1;
//                Log.d("タイマー","時間経過");
//            }
//        }, start_time);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
//
//        sdma=new SimpleDateFormat("yyyy.MM.dd");
//        sdmb=new SimpleDateFormat("HH:mm:ss:SSS");
//
//        initFile();
//        connect();
//
////        //        タイマー
////        final Handler handler = new Handler();
////        handler.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                start_flag = 1;
////                Log.d("タイマー","時間経過");
////            }
////        }, start_time);
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult connectionResult) {
//                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
//                    }
//                })
//                .addApi(Wearable.API)
//                .build();
//        mGoogleApiClient.connect();
//
////        再起処理
////    心拍数の判断用flag
//        flag2 = 0;
////    起動してからのフラグ
//        start_flag = 0;
//
//        //        タイマー
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                start_flag = 1;
//                Log.d("タイマー","時間経過");
//            }
//        }, start_time);


        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("My Heart Rate")
                .setContentText("計測中…")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(startId, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        Toast.makeText(this, "MyService#onDestroy", Toast.LENGTH_SHORT).show();


        close();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");




        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    // wearから受け取ったデータを基に分岐
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived : " + messageEvent.getPath());
        msg = messageEvent.getPath();
        double d = parseDouble(msg);
//        writeFile(msg);
//        send();

//        眠いかの判定
        judgeSleep(d);
    }

//    心拍情報が閾値を超えたかを判定
    public void judgeSleep(double d) {
        Log.d("心拍確認", "送られてきたよ");


//        if (flag2 == 0 && d < Threashold && start_flag == 1) {
////        if (flag2 == 0 && d < Threashold) {
//            flag2 = 1;
//            start_flag = 2;
//            Log.d("Intent遷移確認","遷移するよ");
//            //Toast.makeText(this, "Intent遷移", Toast.LENGTH_SHORT).show();
//            Intent intentParam = new Intent();
//            intentParam.setClass(this, MediaActivity.class);
//            //            intentParam.putExtra(OPTION_SERVER_TYPE, _iSelectedType);
//            intentParam.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intentParam);
//        }


        //Toast.makeText(this, "Intent遷移", Toast.LENGTH_SHORT).show();
        Intent intentParam = new Intent();
        intentParam.setClass(this, MediaActivity.class);
        //            intentParam.putExtra(OPTION_SERVER_TYPE, _iSelectedType);
        intentParam.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentParam);
    }





    // 初回起動時に呼び出される，ファイルの作成
    public void initFile(){
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/"+fileName;

        System.out.println(filePath);

        Log.d(TAG,filePath);

        File file = new File(filePath);
        file.getParentFile().mkdir();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ファイル書き出し
    public void writeFile(String text){
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/"+fileName;

        File file = new File(filePath);
        file.getParentFile().mkdir();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            // 秒数を取得
            nowTimeMillis=System.currentTimeMillis();
            if (startTimeMillis==0){
                time=0;
                startTimeMillis=System.currentTimeMillis();
            }else{
                time=nowTimeMillis-startTimeMillis;
            }

            // 書き出し内容（年月日，時分秒ミリ秒，ボタン押してからの秒，心拍数）
            bw.write(sdma.format(new Date())+","+sdmb.format(new Date())+","+Double.toString(time/1000)+","+text+"\n");
            bw.flush();
            bw.close();
            Log.d(TAG,"WriteFile");
        } catch (Exception e) {
            Log.d(TAG,"CantWriteFile");
            e.printStackTrace();
        }
    }

    // ソケット作成
    public void connect(){
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    s= new MySocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
            @Override
            protected void onPostExecute(String result){

            }
        }.execute();
    }
    // Wearにメッセージ（String）を送信
    public void send(){
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (s!=null){
                        s.send(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
            @Override
            protected void onPostExecute(String result){

            }
        }.execute();
    }
    // ソケットをクローズ
    public void close(){
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (s!=null){
                        s.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
            @Override
            protected void onPostExecute(String result){

            }
        }.execute();
    }
}
