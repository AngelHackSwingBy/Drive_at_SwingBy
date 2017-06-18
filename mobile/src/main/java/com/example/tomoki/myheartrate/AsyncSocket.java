package com.example.tomoki.myheartrate;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by takayayuuki on 2017/06/17.
 */

public class AsyncSocket extends AsyncTask<String, Integer, String> {

    private AsyncCallback _asyncCallback = null;

    private String peerID;

    public AsyncSocket(AsyncCallback asyncCallback) {
        this._asyncCallback = asyncCallback;
    }

    protected String doInBackground(String... url) {

        if(url[0].equals("1")) {

            Socket socket = null;
            BufferedReader reader = null;
            try {
                // サーバーへ接続
                socket = new Socket(url[1], Integer.valueOf(url[2]));

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

                pw.println("1," + url[3]);

                // メッセージ取得オブジェクトのインスタンス化
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));

                // サーバーからのメッセージを受信
                String message = reader.readLine();

                // 接続確認
                if (!(message != null)) {
                    System.out.println("接続失敗");
                    peerID = "";

                } else {
                    System.out.println("接続成功");
                    System.out.println(message);

                    peerID = message;
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    // 接続終了処理
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return peerID;
        }else if(url[0].equals("0")){
            Socket socket = null;
            BufferedReader reader = null;
            try {
                // サーバーへ接続
                socket = new Socket(url[1], Integer.valueOf(url[2]));

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

                pw.println("0," +url[3]+","+url[4]+","+url[5]+","+url[6]);

                // メッセージ取得オブジェクトのインスタンス化
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));

                // サーバーからのメッセージを受信
                String message = reader.readLine();

                // 接続確認
                if (!(message != null)) {
                    System.out.println("接続失敗");

                } else {
                    System.out.println("接続成功");
                    System.out.println(message);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    // 接続終了処理
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }else {
            Socket socket = null;
            BufferedReader reader = null;
            try {
                // サーバーへ接続
                socket = new Socket(url[1], Integer.valueOf(url[2]));

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

                pw.println("2," +url[3]);

                // メッセージ取得オブジェクトのインスタンス化
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));

                // サーバーからのメッセージを受信
                String message = reader.readLine();

                // 接続確認
                if (!(message != null)) {
                    System.out.println("接続失敗");

                } else {
                    System.out.println("接続成功");
                    System.out.println(message);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    // 接続終了処理
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this._asyncCallback.onPreExecute();
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this._asyncCallback.onProgressUpdate(values[0]);
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        this._asyncCallback.onPostExecute(result);
    }

    protected void onCancelled() {
        super.onCancelled();
        this._asyncCallback.onCancelled();
    }
}
