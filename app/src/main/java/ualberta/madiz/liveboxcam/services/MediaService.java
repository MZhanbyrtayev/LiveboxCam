package ualberta.madiz.liveboxcam.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String MEDIA_STREAM_URL = "http://192.168.1.100/stories/getFile/";
    private static final String TAG = "AudioService";
    MediaPlayer mediaPlayer;
    Context appContext;
    AudioAttributes.Builder audioBuilder;

    public MediaService() {
        Log.d(TAG, "Def");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioBuilder = new AudioAttributes.Builder();
        audioBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
        audioBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);

        Log.d(TAG, "Start command");
        try{
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(audioBuilder.build());
//            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(MEDIA_STREAM_URL);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            //mediaPlayer.reset();
            mediaPlayer.prepareAsync();
            Log.d(TAG,"PrepareAsync");
        }catch (IOException me){
            Log.d(TAG, me.getLocalizedMessage());
        }

        Log.d(TAG, "Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            Log.d(TAG,"Released");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG,"Run");
        mp.start();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }
}
