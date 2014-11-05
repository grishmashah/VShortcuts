package com.whoopeelab.vshortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class ShortcutVoiceReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.logInfo(getClass().getName(), "inside onReceive() yay!...");
        SharedPreferences sharedPref = context.getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("VSHORTCUT_VOl_CTRL_ON", true)) {
          Utils.logInfo(getClass().getName(), "vshortcut on.. starting shortcut activity...");
          AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
          int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -10);
          if(streamType == AudioManager.STREAM_RING && !audioManager.isMusicActive() && audioManager.getMode() == AudioManager.MODE_NORMAL) {
            Intent actIntent = new Intent(context, ShortcutVoiceActivity.class);
            actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(actIntent);
          }
        } else {
          Utils.logInfo(getClass().getName(), "vshortcut off.. not starting shortcut activity...");
        }
    }
}