package com.whoopeelab.vshortcuts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class ShortcutVoiceActivity extends Activity {

  void speechRecognizerStart() {
    Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
    Utils.logInfo(getClass().getName(), "before start activity result" + Calendar.getInstance().getTimeInMillis());
    startActivityForResult(recognizerIntent, 1);
    Utils.logInfo(getClass().getName(), "start speech recognizer now" + Calendar.getInstance().getTimeInMillis());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Utils.logInfo(getClass().getName(), "in oncreate of invisible activity");
    speechRecognizerStart();
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Utils.logInfo(getClass().getName(), "Inside Activity Result: " + Calendar.getInstance().getTimeInMillis());
    if(requestCode == 1) {
      if (resultCode == RESULT_OK && null != data) {
        ArrayList<String> speechToTextResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE); //getPreferences(Context.MODE_MULTI_PROCESS);
        boolean found = false;
        for(String shortcut: speechToTextResult) {
            shortcut = shortcut.toUpperCase();
            if(sharedPref.contains(shortcut)) {
                found = true;
                Utils.logInfo(getClass().getName(), "Shortcut is: " + shortcut);
                DataModel infoData = new DataModel(sharedPref.getString(shortcut, null));
                if(infoData.isApplicationType()) {
                  Intent intent = getPackageManager().getLaunchIntentForPackage(infoData.getActivityCallable());
                  if(intent != null) {
                    startActivity(intent);
                  } else {
                    Toast t = Toast.makeText(this, "No App Shortcut Found for [" + speechToTextResult.get(0) + "]", 5000);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                  }
                } else if(infoData.isContactType()) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + infoData.getActivityCallable()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        }
        if(!found) {
            Toast t = Toast.makeText(this, "No App Shortcut Found for [" + speechToTextResult.get(0) + "]", 5000);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
      }
    }
    finish();
    return;
  }
}