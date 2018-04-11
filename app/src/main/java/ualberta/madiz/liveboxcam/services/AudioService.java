package ualberta.madiz.liveboxcam.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;

public class AudioService extends Service implements MediaPlayer.OnPreparedListener{
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String MEDIA_STREAM_URL = "http://192.168.137.1/stories/getAudio/";
    private static final String TAG = "AudioService";
    MediaPlayer mediaPlayer = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equalsIgnoreCase(ACTION_PLAY)){
            try{
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(MEDIA_STREAM_URL);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
            }catch (IOException me){
                Log.d(TAG, me.getLocalizedMessage());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}
