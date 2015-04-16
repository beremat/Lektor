package com.nowakm.lektor;

import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Marcin on 4/15/2015.
 */
public class RecordFragment extends Fragment {

    static boolean recording = false;
    static TimeCounter timeCounter;
    static MediaRecorder recorder;
    static String recordingFileName = "test.3gp";
    static boolean fancyButton = true;
    static boolean recorderInit = false;
    static int amplitude;
    static int maxAmplitude = 0;
    static long lastMaxAmplitude;
    float normalizationFactor = 1.0f;
    float prevNormalizationFactor = 1.0f;
    int[] smoothSamples = new int[SAMPLES_TO_CONSIDER];
    int sampleIndex = 0;

    // begin user-customizable settings
    static boolean normalizeAmplitude = true;
    static boolean rescaleNormalizedAmplitude = true;
    static boolean smoothAmplitudeDisplay = true;
    static int STATUS_UPDATE_FREQ = 10; //frames per second
    static int BUTTON_DRAW_FREQ = 30;   //frames per second
    static int RESCALING_INTERVAL = 15; //seconds
    static int SAMPLES_TO_CONSIDER = 20;

    /**
     * The fragment argument representing the section number for this
     * fragment
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number
     */
    public static RecordFragment newInstance(int sectionNumber) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RecordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        MainActivity.recordStatus = (TextView)rootView.findViewById(R.id.record_status_text);
        final ImageView recordButton = (ImageView)rootView.findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = !recording;
                if (recording) {
                    if (fancyButton)
                        recordButton.setImageResource(R.drawable.record_button_clip_outline);
                    else
                        recordButton.setImageResource(R.drawable.record_button_active);

                    //thread to handle updating duration
                    new Thread(new Runnable() {
                        public void run() {
                            timeCounter = new TimeCounter();
                            lastMaxAmplitude = timeCounter.getTimePassed();
                            while (recording) {
                                MainActivity.runOnUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.recordStatus.setText(timeCounter.getFormattedTimePassed());
                                    }
                                });
                                try {
                                    Thread.sleep(1000/STATUS_UPDATE_FREQ);
                                }
                                catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    //thread to handle MediaRecorder
                    new Thread(new Runnable() {
                        public void run() {
                            recorder = new MediaRecorder();
                            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            recorder.setAudioChannels(1);
                            String filesDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                            filesDir += "/Android/data/";
                            filesDir += MainActivity.appContext.getPackageName();
                            File storageDir = new File(filesDir);
                            if (!storageDir.exists()) {
                                storageDir.mkdir();
                                System.out.println("Created public storage directory: " + filesDir);
                            } else
                                System.out.println("Public storage directory already exists: " + filesDir);
                            recorder.setOutputFile(filesDir + "/" + recordingFileName);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                            try {
                                recorder.prepare();
                                recorder.start();
                                recorderInit = true;
                                prevNormalizationFactor = 1.0f;
                                normalizationFactor = 1.0f;
                                maxAmplitude = 0;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    //thread to handle fancy button FX
                    if (fancyButton) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    //to prevent calling getMaxAmplitude on uninitialized recorder
                                    while (!recorderInit)
                                        Thread.sleep(50);
                                    final LayerDrawable fancyButton = (LayerDrawable)recordButton.getDrawable();
                                    final ClipDrawable buttonClipper = (ClipDrawable)fancyButton.findDrawableByLayerId(R.id.button_clip);
                                    while (recording) {
                                        amplitude = recorder.getMaxAmplitude();
                                        if (amplitude != 0)
                                            MainActivity.runOnUI(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (normalizeAmplitude && amplitude > maxAmplitude) {
                                                        maxAmplitude = amplitude;
                                                        prevNormalizationFactor = normalizationFactor;
                                                        normalizationFactor = 32767 / maxAmplitude;
                                                        lastMaxAmplitude = timeCounter.getTimePassed();
                                                    }
                                                    if (smoothAmplitudeDisplay) {
                                                        if (normalizeAmplitude)
                                                            smoothSamples[sampleIndex] = (int)(amplitude*normalizationFactor);
                                                        else
                                                            smoothSamples[sampleIndex] = amplitude;
                                                        sampleIndex++;
                                                        if (sampleIndex == SAMPLES_TO_CONSIDER)
                                                            sampleIndex = 0;
                                                        amplitude = 0;
                                                        for (int i : smoothSamples)
                                                            amplitude += i;
                                                        amplitude /= SAMPLES_TO_CONSIDER;
                                                    }
                                                    else if (normalizeAmplitude)
                                                        amplitude *= normalizationFactor;
                                                    //maximum value returned by getMaxAmplitude() is 32767; max value of setLevel is 10000
                                                    buttonClipper.setLevel((int)(amplitude/3.2767));
                                                }
                                            });
                                        Thread.sleep(1000 / BUTTON_DRAW_FREQ);
                                    }
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    //thread to handle rescaling of normalization
                    if (rescaleNormalizedAmplitude) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    normalizeAmplitudeRescaler();
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                if (!recording) {
                    recordButton.setImageResource(R.drawable.record_button);
                    recorder.stop();
                    //reset() to prevent "MediaRecorder went away with unhandled events"
                    recorder.reset();
                    recorder.release();
                    recorder = null;
                    recorderInit = false;
                }
            }
        });
        return rootView;
    }

    // TODO: debug and fix the normalization rescaler
    public void normalizeAmplitudeRescaler() throws InterruptedException {
        while (recording) {
            Thread.sleep(1000);
            if (timeCounter.getTimePassed() - lastMaxAmplitude > RESCALING_INTERVAL*1000) {
                if (normalizationFactor == prevNormalizationFactor) {
                    normalizationFactor *= 2;
                    prevNormalizationFactor *= 2;
                    maxAmplitude /= 2;
                    lastMaxAmplitude = timeCounter.getTimePassed();
                }
                else {
                    normalizationFactor = prevNormalizationFactor;
                    maxAmplitude *= normalizationFactor / prevNormalizationFactor;
                }
            }
        }
    }
}
