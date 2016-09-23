package mumayank.shortmeetings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // for testing set to true
    private boolean fasterExecution = false;

    // vars
    private boolean meetingMode;
    private boolean soundLoaded = false;
    private int soundID;
    private SoundPool soundPool;
    private boolean soundPlayedOnce = false;
    CountDownTimer countDownTimer;

    // constants
    private final int DEFAULT_MEETING_MINS = 10;
    private final int DEFAULT_SCRUM_MINS = 2;

    // Views to be binded
    private SeekBar durationSeekBar;
    private TextView durationTextView, timeTextView, sendEmailTextView,
            addTime1TextView, addTime2TextView, addTime5TextView, addTime10TextView,
            startTextView, endTextView, nextTextView, resetTextView;
    private LinearLayout progressLayout;
    private RadioButton radioMeeting, radioScrum;

    private LinearLayout radioParentLayout, durationParentLayout, timeParentLayout,
            addTimeParentLayout, endNextParentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSupportActionBar();
        bindViews();
        resetViews();
        defineOnClicks();

    }

    private void hideSupportActionBar() {
        getSupportActionBar().hide();
    }

    private void bindViews() {
        durationSeekBar = (SeekBar) findViewById(R.id.durationSeekBar);
        durationTextView = (TextView) findViewById(R.id.durationTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        sendEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
        addTime1TextView = (TextView) findViewById(R.id.addTime1);
        addTime2TextView = (TextView) findViewById(R.id.addTime2);
        addTime5TextView = (TextView) findViewById(R.id.addTime5);
        addTime10TextView = (TextView) findViewById(R.id.addTime10);
        startTextView = (TextView) findViewById(R.id.startButton);
        endTextView = (TextView) findViewById(R.id.endButton);
        nextTextView = (TextView) findViewById(R.id.nextButton);
        resetTextView = (TextView) findViewById(R.id.resetTextView);
        progressLayout = (LinearLayout) findViewById(R.id.progressLayout);
        radioMeeting = (RadioButton) findViewById(R.id.radioMeeting);
        radioScrum = (RadioButton) findViewById(R.id.radioScrum);

        radioParentLayout = (LinearLayout) findViewById(R.id.radioParentLayout);
        durationParentLayout = (LinearLayout) findViewById(R.id.durationParentLayout);
        timeParentLayout = (LinearLayout) findViewById(R.id.timeParentLayout);
        addTimeParentLayout = (LinearLayout) findViewById(R.id.addTimeParentLayout);
        endNextParentLayout = (LinearLayout) findViewById(R.id.endNextParentLayout);
    }

    private void resetViews() {

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        playSound(false);

        setDurationSeekBarVal(10);

        ViewGroup.LayoutParams layoutParams = progressLayout.getLayoutParams();
        layoutParams.width = 0;
        progressLayout.setLayoutParams(layoutParams);

        radioMeeting.setEnabled(true);
        radioScrum.setEnabled(true);
        durationSeekBar.setEnabled(true);

        addTime5TextView.setVisibility(View.VISIBLE);
        addTime10TextView.setVisibility(View.VISIBLE);
        nextTextView.setVisibility(View.VISIBLE);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioParentLayout);
        radioGroup.clearCheck();

        // hide views
        durationParentLayout.setVisibility(View.GONE);
        startTextView.setVisibility(View.GONE);
        timeParentLayout.setVisibility(View.GONE);
        addTimeParentLayout.setVisibility(View.GONE);
        endNextParentLayout.setVisibility(View.GONE);
    }

    private void defineOnClicks() {
        defineOnClickSeekBar();
        defineOnClickRadioButtons();
        defineOnClickButtons();
        defineOnClickAddTime();
    }

    private void defineOnClickSeekBar() {
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setDurationTextView();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void defineOnClickRadioButtons() {
        radioMeeting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    meetingMode = true;
                    setDurationSeekBarVal(DEFAULT_MEETING_MINS);
                    radioSelectProceed();
                }
            }
        });

        radioScrum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    meetingMode = false;
                    setDurationSeekBarVal(DEFAULT_SCRUM_MINS);
                    radioSelectProceed();
                }
            }
        });
    }

    private void defineOnClickButtons() {

        startTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioMeeting.setEnabled(false);
                radioScrum.setEnabled(false);
                durationSeekBar.setEnabled(false);
                startTextView.setVisibility(View.GONE);

                timeParentLayout.setVisibility(View.VISIBLE);
                endNextParentLayout.setVisibility(View.VISIBLE);

                if (meetingMode == false) {
                    addTime5TextView.setVisibility(View.GONE);
                    addTime10TextView.setVisibility(View.GONE);
                    nextTextView.setVisibility(View.VISIBLE);
                } else {
                    nextTextView.setVisibility(View.GONE);
                }

                timeTextView.setText(getDurationSeekBarVal() + "");

                startTimer();
            }
        });

        endTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Sure to end?")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("END", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetViews();
                            }})
                        .setNegativeButton("CANCEL", null).show();
            }
        });

        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTimeParentLayout.setVisibility(View.GONE);
                playSound(false);
                timeTextView.setText(getDurationSeekBarVal() + "");
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                startTimer();
            }
        });

        resetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Sure to reset?")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetViews();
                            }})
                        .setNegativeButton("CANCEL", null).show();
            }
        });

        sendEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {"mumayank@ireff.in", "mumayank@gmail.com"};
                //String[] CC = {""};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                //emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding 'A Short Meeting' Android App");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey Mayank,\n\nMy message is  ");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email to developer"));
                }
                catch (android.content.ActivityNotFoundException ex) {
                    //Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void defineOnClickAddTime() {
        addTime1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime(1);
            }
        });

        addTime2TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime(2);
            }
        });

        addTime5TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime(5);
            }
        });

        addTime10TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime(10);
            }
        });
    }

    private void addTime(int time) {
        addTimeParentLayout.setVisibility(View.GONE);
        timeTextView.setText(time + "");
        startTimer();
    }

    private void setDurationSeekBarVal(int val) {
        durationSeekBar.setProgress(val - 1);
        setDurationTextView();
    }

    private int getDurationSeekBarVal() {
        return (durationSeekBar.getProgress() +  1);
    }

    private void setDurationTextView() {
        durationTextView.setText( ( (int) durationSeekBar.getProgress() + 1) + " m");
    }

    private void radioSelectProceed() {
        durationParentLayout.setVisibility(View.VISIBLE);
        startTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Sure to exit?")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }})
                .setNegativeButton("CANCEL", null).show();
    }

    private void loadSound() {
        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Load the sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                soundLoaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.watch, 1);
    }

    private void startTimer() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playSound(false);

        int divider = 1;
        if (fasterExecution) {
            divider = 10;
        }

        final int totalMillis = Integer.parseInt(timeTextView.getText().toString() + "") * 60 * (1000 / divider); // TODO to test, make it 10 or 100

        final int finalDivider = divider;
        countDownTimer = new CountDownTimer(totalMillis, 1) {

            public void onTick(long millisUntilFinished) {

                int roundedMillisUntilFinished = (int) (millisUntilFinished/100);

                timeTextView.setText( (int) ((roundedMillisUntilFinished / (600 / finalDivider)) + 1) + "");

                ViewGroup.LayoutParams layoutParams = progressLayout.getLayoutParams();
                int deviceWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
                float ratio = (float) (totalMillis - millisUntilFinished) / totalMillis;
                layoutParams.width = (int) (deviceWidth * ratio);
                progressLayout.setLayoutParams(layoutParams);
            }

            public void onFinish() {
                playSound(true);
                addTimeParentLayout.setVisibility(View.VISIBLE);
                timeTextView.setText("0");

                if (meetingMode == false) {
                    nextTextView.setVisibility(View.VISIBLE);
                }
            }

        }.start();
    }

    private void playSound(boolean play) {

        // Getting the user sound settings
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;

        if (play) {
            if (soundLoaded) {
                if (soundPlayedOnce) {
                    soundPool.resume(soundID);
                } else {
                    soundPool.play(soundID, volume, volume, 1, -1, 1f);
                    soundPlayedOnce = true;
                }
            }
        } else {
            if (soundLoaded) {
                soundPool.pause(soundID);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSound();
    }

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playSound(false);
        soundPool.release();
        super.onStop();
    }
}
