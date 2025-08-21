package com.example.CARPOOL.DeviceHandlers.Online;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.media.MediaRecorder;
import android.media.AudioRecord;
import android.media.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.R;



/**
 * Activity to get the share from audio interaction and demodulate the audio signal to
 * get back the binary data.
 */
public class AudioActivity extends AppCompatActivity {

    private Button button;
    private static final String TAG = "AudioActivity";

    private static final int RECORDER_SAMPLERATE     = 44100;
    private static final int RECORDER_CHANNELS       = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

    private AudioRecord recorder = null;
    private Thread audioThread   = null;
    private boolean isRecording  = false;

    short[] audioBuffer = new short[bufferSize*10];
    int secret[]        = new int[128];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        setTitle("Interact with audio device");

        button = findViewById(R.id.audio_button);
        button.setText("Listen");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button.getText().equals("Listen")) {
                    listen();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    button.setText("Stop");
                } else if (button.getText().equals("Stop")) {
                    stop();
                }
            }
        });
    }

    private void listen() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
                bufferSize*10);
        recorder.startRecording();
        isRecording = true;
        audioThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                int k   = 0;
                int one = 0;
                int zro = 0;

                while(isRecording)
                {
                    double readSize = recorder.read(audioBuffer, 0,
                            bufferSize*10);
                    for (int i=7500 ; i<readSize ; i=i+3330)
                    {
                        for (int j=i ; j<i+500 ; j++)
                        {
                            if (audioBuffer[j] != 0)
                                one++;
                            else
                                zro++;
                        }
                        for (int j=i ; j>i-500 ; j--)
                        {
                            if (audioBuffer[j] != 0)
                                one++;
                            else
                                zro++;
                        }
                        if(one>zro)
                            secret[k++] = 1;
                        else
                            secret[k++] = 0;
                        one = 0;
                        zro = 0;
                    }
                }
            }
        });
        audioThread.start();
    }

    private void stop() {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recorder = null;
        audioThread = null;

        Log.d(TAG, Arrays.toString(secret));

        Intent resultIntent = new Intent();
        resultIntent.putExtra("share", Arrays.toString(secret));
        setResult(RESULT_OK,resultIntent);

        finish();
    }


    private String getRecordingFilePath()
    {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "carpoolAudioShare" + ".wav");
        return file.getPath();
    }
}