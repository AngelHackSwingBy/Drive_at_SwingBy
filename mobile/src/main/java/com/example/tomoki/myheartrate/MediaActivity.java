package com.example.tomoki.myheartrate;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


/**
 *
 */
public class MediaActivity
        extends Activity
{
    private static final String TAG = MediaActivity.class.getSimpleName();

    private Peer _peer;
    private String _peer2;
    private MediaConnection _media;

    private MediaStream _msLocal;
    private MediaStream _msRemote;

    private Handler _handler;

    private String   _id;
    private String[] _listPeerIds;
    private boolean  _bCalling;

    private String peerID;


    private int judge_sleep = 0;

    private Timer timer;
    private MainTimerTask maintimerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Window wnd = getWindow();
        wnd.addFlags(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_video_chat);

        _handler = new Handler(Looper.getMainLooper());
        Context context = getApplicationContext();

        //////////////////////////////////////////////////////////////////////
        //////////////////  START: Initialize SkyWay Peer ////////////////////
        //////////////////////////////////////////////////////////////////////

        // Please check this page. >> https://skyway.io/ds/
        PeerOption options = new PeerOption();

        //Enter your API Key.
        options.key = "db5c5736-4642-4c61-9cbd-ce73e0f1785b";
        //Enter your registered Domain.
        options.domain = "localhost";




        // SKWPeer has many options. Please check the document. >> http://nttcom.github.io/skyway/docs/

        _peer = new Peer(context, options);


        //司頼みます

        AsyncSocket asyncSocket = new AsyncSocket(new AsyncCallback() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(String result) {
                System.out.println(result);
                peerID = result;
            }

            @Override
            public void onProgressUpdate(int progress) {
            }

            @Override
            public void onCancelled() {
            }
        });

            try {
                _peer2 = asyncSocket.execute("1","59.106.219.4","44344",String.valueOf(_peer)).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }



        //Toast.makeText(this,_peer2,Toast.LENGTH_LONG).show();






    //////////////////


        setPeerCallback(_peer);

        //////////////////////////////////////////////////////////////////////
        ////////////////// END: Initialize SkyWay Peer ///////////////////////
        //////////////////////////////////////////////////////////////////////





        //request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},0);
        }else{
            startLocalStream();
        }




        _bCalling = false;
//        startLocalStream();


//
//        _handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if(_peer2 != "no_peer"){
//                    calling(_peer2);
//                }
//
//            }
//        });
//        calling(_peer2);

        //
        // Initialize views
        //
        Button btnAction = (Button) findViewById(R.id.btnAction);
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setEnabled(false);

                if (!_bCalling)
                {
                    listingPeers();
                }
                else
                {
                    closing();
//                    startService(new Intent(MediaActivity.this, MyService.class));
                }

                v.setEnabled(true);
            }
        });

        //
        Button switchCameraAction = (Button)findViewById(R.id.switchCameraAction);
        switchCameraAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(null != _msLocal){
                    Boolean result = _msLocal.switchCamera();
                    if(true == result)
                    {
                        //Success
                    }else
                    {
                        //Failed
                    }
                }

            }
        });

        this.timer = new Timer();
        this.maintimerTask = new MainTimerTask();
        this.timer.schedule(maintimerTask,5000);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocalStream();
                }else{
                    Toast.makeText(this,"Failed to access the camera and microphone.\nclick allow when asked for permission.",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    void startLocalStream(){
        Navigator.initialize(_peer);
        MediaConstraints constraints = new MediaConstraints();
        _msLocal = Navigator.getUserMedia(constraints);

        Canvas canvas = (Canvas) findViewById(R.id.svSecondary);
        canvas.addSrc(_msLocal, 0);
    }



    /**
     * Media connecting to remote peer.
     * @param strPeerId Remote peer.
     */
    void calling(String strPeerId)
    {
        //////////////////////////////////////////////////////////////////////
        ////////////////// START: Calling SkyWay Peer   //////////////////////
        //////////////////////////////////////////////////////////////////////

        if (null == _peer)
        {
            return;
        }

        if (null != _media)
        {
            _media.close();
            _media = null;
        }

        CallOption option = new CallOption();

        if(String.valueOf(_peer2).equals("1")){
//            //Toast.makeText(this, "Calling = true", Toast.LENGTH_SHORT).show();
//            _media = _peer.call(strPeerId, _msLocal, option);
        }else{
            //Toast.makeText(this, "Calling = true", Toast.LENGTH_SHORT).show();
            _media = _peer.call(strPeerId, _msLocal, option);
        }

//        //Toast.makeText(this, _peer2, Toast.LENGTH_SHORT).show();


        if (null != _media)
        {
            setMediaCallback(_media);
            if(String.valueOf(_peer2).equals("1")){
//                //Toast.makeText(this, "bCalling = true", Toast.LENGTH_SHORT).show();
//                _bCalling = true;
            }else{
                //Toast.makeText(this, "bCalling = true", Toast.LENGTH_SHORT).show();
                _bCalling = true;
            }

        }

        //////////////////////////////////////////////////////////////////////
        /////////////////// END: Calling SkyWay Peer   ///////////////////////
        //////////////////////////////////////////////////////////////////////


        updateUI();
    }



    //////////Start:Set Peer callback////////////////
    ////////////////////////////////////////////////
    private void setPeerCallback(Peer peer)
    {
        //////////////////////////////////////////////////////////////////////////////////
        ///////////////////// START: Set SkyWay peer callback   //////////////////////////
        //////////////////////////////////////////////////////////////////////////////////

        // !!!: Event/Open
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Open]");

                if (object instanceof String)
                {
                    _id = (String) object;
                    Log.d(TAG, "ID:" + _id);

                    updateUI();
                }
            }
        });

        // !!!: Event/Call
        peer.on(Peer.PeerEventEnum.CALL, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Call]");
                if (!(object instanceof MediaConnection))
                {
                    return;
                }

                _media = (MediaConnection) object;

                _media.answer(_msLocal);

                setMediaCallback(_media);

                _bCalling = true;

                updateUI();
            }
        });

        // !!!: Event/Close
        peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Close]");

                AsyncSocket asyncSocket1 = new AsyncSocket(new AsyncCallback() {
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void onPostExecute(String result) {
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                    }

                    @Override
                    public void onCancelled() {
                    }
                });
                asyncSocket1.execute("2","59.106.219.4","44344",String.valueOf(_peer));
            }
        });

        // !!!: Event/Disconnected
        peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Disconnected]");
            }
        });

        // !!!: Event/Error
        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                PeerError error = (PeerError) object;

                Log.d(TAG, "[On/Error]" + error);

                String strMessage = "" + error;
                String strLabel = getString(android.R.string.ok);

//                MessageDialogFragment dialog = new MessageDialogFragment();
//                dialog.setPositiveLabel(strLabel);
//                dialog.setMessage(strMessage);
//
//                dialog.show(getFragmentManager(), "error");
            }
        });

        //////////////////////////////////////////////////////////////////////////////////
        /////////////////////// END: Set SkyWay peer callback   //////////////////////////
        //////////////////////////////////////////////////////////////////////////////////
    }


    //Unset peer callback
    void unsetPeerCallback(Peer peer)
    {
        peer.on(Peer.PeerEventEnum.OPEN, null);
        peer.on(Peer.PeerEventEnum.CONNECTION, null);
        peer.on(Peer.PeerEventEnum.CALL, null);
        peer.on(Peer.PeerEventEnum.CLOSE, null);
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null);
        peer.on(Peer.PeerEventEnum.ERROR, null);
    }


    void setMediaCallback(MediaConnection media)
    {
        //////////////////////////////////////////////////////////////////////////////////
        //////////////  START: Set SkyWay peer Media connection callback   ///////////////
        //////////////////////////////////////////////////////////////////////////////////

        // !!!: MediaEvent/Stream
        media.on(MediaConnection.MediaEventEnum.STREAM, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                _msRemote = (MediaStream) object;

                Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
                canvas.addSrc(_msRemote, 0);
            }
        });

        // !!!: MediaEvent/Close
        media.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                if (null == _msRemote)
                {
                    return;
                }

                Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
                canvas.removeSrc(_msRemote, 0);

                _msRemote = null;

                _media = null;
                _bCalling = false;

                updateUI();
            }
        });

        // !!!: MediaEvent/Error
        media.on(MediaConnection.MediaEventEnum.ERROR, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                PeerError error = (PeerError) object;

                Log.d(TAG, "[On/MediaError]" + error);

                String strMessage = "" + error;
                String strLabel = getString(android.R.string.ok);
//
//                MessageDialogFragment dialog = new MessageDialogFragment();
//                dialog.setPositiveLabel(strLabel);
//                dialog.setMessage(strMessage);
//
//                dialog.show(getFragmentManager(), "error");
            }
        });

        //////////////////////////////////////////////////////////////////////////////////
        ///////////////  END: Set SkyWay peer Media connection callback   ////////////////
        //////////////////////////////////////////////////////////////////////////////////
    }






    //Unset media connection event callback.
    void unsetMediaCallback(MediaConnection media)
    {
        media.on(MediaConnection.MediaEventEnum.STREAM, null);
        media.on(MediaConnection.MediaEventEnum.CLOSE, null);
        media.on(MediaConnection.MediaEventEnum.ERROR, null);
    }

    // Listing all peers
    void listingPeers()
    {
        if ((null == _peer) || (null == _id) || (0 == _id.length()))
        {
            return;
        }

        _peer.listAllPeers(new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (!(object instanceof JSONArray)) {
                    return;
                }

                JSONArray peers = (JSONArray) object;

                System.out.println("peer:"+peers);

                StringBuilder sbItems = new StringBuilder();
                for (int i = 0; peers.length() > i; i++) {
                    String strValue = "";
                    try {
                        strValue = peers.getString(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (0 == _id.compareToIgnoreCase(strValue)) {
                        continue;
                    }

                    if (0 < sbItems.length()) {
                        sbItems.append(",");
                    }

                    sbItems.append(strValue);
                }

                String strItems = sbItems.toString();
                _listPeerIds = strItems.split(",");

                System.out.println("_listPeerIds[0]:"+_listPeerIds[0]);

                if ((null != _listPeerIds) && (0 < _listPeerIds.length)) {

                    peerID = String.valueOf(_listPeerIds[0]);

                    selectingPeer();
                }
            }
        });

    }

    /**
     * Selecting peer
     */
    void selectingPeer()
    {

        System.out.println("selectingPeerに入った");

        if (null == _handler)
        {
            return;
        }

        _handler.post(new Runnable() {
            @Override
            public void run() {

                System.out.println(peerID);

//                calling(String.valueOf(_listPeerIds[0]));
                calling(String.valueOf(peerID));
            }
        });

//        _handler.post(new Runnable() {
//            @Override
//            public void run() {
//                FragmentManager mgr = getFragmentManager();
//
//                PeerListDialogFragment dialog = new PeerListDialogFragment();
//                dialog.setListener(
//                        new PeerListDialogFragment.PeerListDialogFragmentListener() {
//                            @Override
//                            public void onItemClick(final String item) {
//                                _handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        calling(item);
//                                    }
//                                });
//                            }
//                        });
//                dialog.setItems(_listPeerIds);
//
//                dialog.show(mgr, "peerlist");
//            }
//        });
    }



    /**
     * Closing connection.
     */
    void closing()
    {
        if (false == _bCalling)
        {
            return;
        }

        _bCalling = false;

        if (null != _media)
        {
            _media.close();
        }
    }

    void updateUI()
    {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Button btnAction = (Button) findViewById(R.id.btnAction);
                if (null != btnAction) {
                    if (false == _bCalling) {
                        btnAction.setText("Calling");
                    } else {
                        btnAction.setText("Hang up");
                    }
                }

                TextView tvOwnId = (TextView) findViewById(R.id.tvOwnId);
                if (null != tvOwnId) {
                    if (null == _id) {
                        tvOwnId.setText("");
                    } else {
                        tvOwnId.setText(_id);
                    }
                }
            }
        });
    }


    /**
     * Destroy Peer object.
     */
    private void destroyPeer()
    {
        closing();

        if (null != _msRemote)
        {
            Canvas canvas = (Canvas) findViewById(R.id.svPrimary);
            canvas.removeSrc(_msRemote, 0);

            _msRemote.close();

            _msRemote = null;
        }

        if (null != _msLocal)
        {
            Canvas canvas = (Canvas) findViewById(R.id.svSecondary);
            canvas.removeSrc(_msLocal, 0);

            _msLocal.close();

            _msLocal = null;
        }

        if (null != _media)
        {
            if (_media.isOpen)
            {
                _media.close();
            }

            unsetMediaCallback(_media);

            _media = null;
        }

        Navigator.terminate();

        if (null != _peer)
        {
            unsetPeerCallback(_peer);

            if (false == _peer.isDisconnected)
            {
                _peer.disconnect();
            }

            if (false == _peer.isDestroyed)
            {
                _peer.destroy();
            }

            _peer = null;
        }
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        // Disable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Set volume control stream type to WebRTC audio.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    protected void onPause()
    {
        // Set default volume control stream type.
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

        super.onPause();
    }

    @Override
    protected void onStop()
    {
        // Enable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        destroyPeer();

        _listPeerIds = null;
        _handler = null;

        super.onDestroy();
    }

    public class MainTimerTask extends TimerTask {

        public void run() {
            //ここに定周期で実行したい処理を記述します
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                        if(_peer2.equals("1")){
//                            System.out.println("aaaaaaaaaaa"+String.valueOf(_peer2));
//                            calling(_peer2);
                        }else{
                            System.out.println("aaaaaaaaaaa"+String.valueOf(_peer2));
//                            calling(peerID);
                            listingPeers();
                        }
                    }
                    });
                }
            }).start();

            System.out.println("タイマー入った");

        }
    }
}
