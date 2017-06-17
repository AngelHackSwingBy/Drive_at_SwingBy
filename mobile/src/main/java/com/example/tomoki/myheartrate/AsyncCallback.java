package com.example.tomoki.myheartrate;

/**
 * Created by takayayuuki on 2017/06/17.
 */

public interface AsyncCallback {

    void onPreExecute();
    void onPostExecute(String result);
    void onProgressUpdate(int progress);
    void onCancelled();

}
