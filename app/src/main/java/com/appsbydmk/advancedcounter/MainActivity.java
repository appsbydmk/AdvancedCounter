package com.appsbydmk.advancedcounter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private TextView tvCounter;
    private int counter = 0;
    private FloatingActionButton fbSettings;
    private SharedPreferences mySettings;
    private boolean changeTextColorNewRun, vibrateOnEachCount,
            limitReachedSound, longVibrateOnlimitReached, confirmOnExit;
    private int counterLimit;
    private Button btnCounter;
    private ColorStateList oldColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getPreferenceValues();
        tvCounter = (TextView) this.findViewById(R.id.tv_counter);
        tvCounter.setOnClickListener(this);
        tvCounter.setLongClickable(true);
        tvCounter.setOnLongClickListener(this);
        oldColors = tvCounter.getTextColors();
        btnCounter = (Button) this.findViewById(R.id.btn_counter);
        btnCounter.setOnClickListener(this);
        fbSettings = (FloatingActionButton) this.findViewById(R.id.fb_settings);
        fbSettings.setOnClickListener(this);
        if (changeTextColorNewRun) {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            tvCounter.setTextColor(color);
        }
        Toast.makeText(this, "Long click anywhere on the screen to reveal menu button!", Toast.LENGTH_SHORT).show();
    }

    private void getPreferenceValues() {
        mySettings = PreferenceManager.getDefaultSharedPreferences(this);
        changeTextColorNewRun = mySettings.getBoolean(getResources().
                getString(R.string.key_change_textcolor_newrun), false);
        counterLimit = Integer.parseInt(mySettings.getString(getResources().
                        getString(R.string.key_counter_limit),
                "999"));
        vibrateOnEachCount = mySettings.getBoolean(getResources().
                getString(R.string.key_vibrate_on_each), false);
        limitReachedSound = mySettings.getBoolean(getResources().
                getString(R.string.key_notify_with_sound), false);
        longVibrateOnlimitReached = mySettings.getBoolean(getResources()
                .getString(R.string.key_long_vibrate_limit_reached), false);
        confirmOnExit = mySettings.getBoolean(getResources().
                getString(R.string.key_confirm_on_exit), false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tv_counter:
                this.increaseCounter();
                break;
            case R.id.fb_settings:
                Intent settingsIntent = new Intent(MainActivity.this, PreferenceSettingsActivity.class);
                this.startActivity(settingsIntent);
                break;
            case R.id.btn_counter:
                this.increaseCounter();
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tv_counter:
                if (fbSettings.getVisibility() == View.VISIBLE)
                    fbSettings.setVisibility(View.GONE);
                else
                    fbSettings.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        return true;
    }

    private void increaseCounter() {
        if (counter < counterLimit) {
            counter++;
            tvCounter.setText(Integer.toString(counter));
            if (vibrateOnEachCount) {
                this.vibratePerCount();
            }
        }
        if (counterLimit == counter) {
            if (limitReachedSound) {
                this.playSound();
            }
            if (longVibrateOnlimitReached) {
                longVibrateOnLimitReached();
            }
            Toast.makeText(getBaseContext(), "Limit reached!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getPreferenceValues();
        if (counterLimit < counter) {
            tvCounter.setText(Integer.toString(0));
        }
        if (changeTextColorNewRun) {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            tvCounter.setTextColor(color);
        } else {
            tvCounter.setTextColor(oldColors);
        }
    }

    private void vibratePerCount() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
    }

    private void playSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void longVibrateOnLimitReached() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (confirmOnExit) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                showExitDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Really quit");
        builder.setTitle("Quit");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
