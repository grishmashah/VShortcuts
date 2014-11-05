package com.whoopeelab.vshortcuts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class DialogBuilder {

  AlertDialog.Builder dialogBuilder;
  AlertDialog alertDialog;

  static Context currentContext;

  ArrayList<Button> lettersButtonArray = new ArrayList<Button>();

  public AlertDialog getDialogBuilder(Context c, int res, String letterClicked) {

    MainActivity.selectedLetter = letterClicked;

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      dialogBuilder = new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
    else
      dialogBuilder = new AlertDialog.Builder(c);

    final Activity act = (Activity) c;
    View v = act.getLayoutInflater().inflate(R.layout.dialog, null);
    LinearLayout l = (LinearLayout) v.findViewById(R.id.all_letters_view);

    for(String s : act.getResources().getStringArray(R.array.all_letters)) {
      Button tv = new Button(c);
      tv.setTextSize(25);
      tv.setText(s);
      tv.setPadding(10, 10, 10, 10);
      tv.setBackgroundResource(android.R.drawable.button_onoff_indicator_off);

      if(s.equals(letterClicked))
        tv.setBackgroundResource(android.R.drawable.button_onoff_indicator_on);

      lettersButtonArray.add(tv);

      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Button viewButton = (Button) view;
          viewButton.setBackgroundResource(android.R.drawable.button_onoff_indicator_on);
          MainActivity.selectedLetter = viewButton.getText().toString();
          for(Button b: lettersButtonArray) {
            Utils.logInfo(getClass().getName(), "Setting setenabled for = " + b.getText());
            if(!b.getText().equals(viewButton.getText()))
              b.setBackgroundResource(android.R.drawable.button_onoff_indicator_off);
          }
        }
      });

      tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      l.addView(tv);
    }

    dialogBuilder.setView(v);
    ListView listview = (ListView) v.findViewById(R.id.dialog_listView);
    ArrayAdapter adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, act.getResources().getStringArray(res));
    listview.setAdapter(adapter);
    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        alertDialog.dismiss();
        // set the selected Letter in MainActivity for using it later
        switch (position){
          case 0:
            Intent intent = new Intent(currentContext, AppsActivity.class);
            act.startActivityForResult(intent, 0);
            break;
          case 1:
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            act.startActivityForResult(pickContactIntent, 1);
            break;
          case 2:
            MainActivity m = (MainActivity) currentContext;
            m.delShortcut();
            break;
        }
      }
    });

    currentContext = c;
    alertDialog = dialogBuilder.create();
    return alertDialog;
  }
}