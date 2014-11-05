package com.whoopeelab.vshortcuts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

  static final Map<String , String> DEFAULT_SHORTCUTS = new HashMap<String , String>() {{
      put("B","com.android.browser");
      put("C","com.sec.android.app.camera");
      put("F","com.facebook.katana");
      put("G","com.google.android.gm");
      put("P","com.android.vending");
      put("M","com.google.android.apps.maps");
      put("Y","com.google.android.youtube");
      put("W","com.whatsapp");
  }};

  Map<String, DataModel> shortcutToModelMap = new HashMap<String, DataModel>();

  ListView mainListView;
  List<RowItem> mainRowItems;
  CustomListViewAdapter mainListAdapter;

  static String selectedLetter = null;
  static View selectedView = null;

  public class LetterComparator implements Comparator<RowItem> {
    @Override
    public int compare(RowItem o1, RowItem o2) {
      return o1.getLetter().compareTo(o2.getLetter());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mainRowItems = new ArrayList<RowItem>();

    PackageManager pm = getPackageManager();
    String [] all_letters = getResources().getStringArray(R.array.all_letters);
    ApplicationInfo packageInfo;

    // read the previously stored info
    SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    boolean firstTime = sharedPref.getBoolean("FIRST_TIME", true);

    if(firstTime) {
      for (Map.Entry<String, String> entry : DEFAULT_SHORTCUTS.entrySet()) {
        packageInfo = getPackageAppInfo(entry.getValue());
          if(packageInfo != null) {
            String shortcutLetter = entry.getKey();
            String appPackageName = entry.getValue();
            String appLoadName = packageInfo.loadLabel(pm).toString();

            DataModel packageData = new DataModel(shortcutLetter, appLoadName, "application", appPackageName, "null");
            RowItem item = new RowItem(shortcutLetter, appLoadName, packageInfo.loadIcon(pm));
            mainRowItems.add(item);
            shortcutToModelMap.put(shortcutLetter, packageData);
            editor.putString(shortcutLetter, packageData.serialize());
          }
        }
     } else {
       for (String letter : all_letters){
         if(sharedPref.contains(letter)) {
           DataModel infoData = new DataModel(sharedPref.getString(letter, null));
           RowItem item = null;
           if ((infoData.isApplicationType())) {
              packageInfo = getPackageAppInfo(infoData.getActivityCallable());
              if(packageInfo != null) {
                item = new RowItem(letter, infoData.getShowName(), packageInfo.loadIcon(pm));
              } else {
                 editor.remove(letter);
              }
           } else if(infoData.isContactType()){
              ImageView iv = new ImageView(this);
              if(infoData.getImageUri() != null)
                 iv.setImageURI(Uri.parse(infoData.getImageUri()));
              else
                 iv.setImageResource(R.drawable.ic_action_user);
              item = new RowItem(letter, infoData.getShowName(), iv.getDrawable());
           }
           if(item != null) {
              Utils.logInfo(getClass().getName(), "Adding letter from shared pref" + letter);
              mainRowItems.add(item);
              shortcutToModelMap.put(letter, infoData);
           }
         }
       }
     }

    // change the first time to false
    editor.putBoolean("FIRST_TIME", false);
    editor.commit();

    // set the ui list view
    mainListView = (ListView) findViewById(R.id.main_list);
    Collections.sort(mainRowItems, new LetterComparator());
    mainListAdapter = new CustomListViewAdapter(this, R.layout.main_list_item, mainRowItems);
    mainListView.setAdapter(mainListAdapter);
    mainListView.setOnItemLongClickListener(this);
    mainListView.setOnItemClickListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    HashMap<String, String> packageToLetterMap = new HashMap<String, String>();
    for(Map.Entry<String, DataModel> entry: shortcutToModelMap.entrySet()) {
      packageToLetterMap.put(entry.getKey(), entry.getValue().getActivityCallable());
    }
    Set<String> removedLetters = new HashSet<String>();

    SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();

    for(Map.Entry<String, String> entry : packageToLetterMap.entrySet()) {
      ApplicationInfo appInfo = getPackageAppInfo(entry.getKey());
      if(appInfo == null) {
        Utils.logInfo(getClass().getName(), "package " + entry.getKey() + " is deleted");
        shortcutToModelMap.remove(entry.getValue());
        removedLetters.add(entry.getValue());
        editor.remove(entry.getValue());
      }
    }
    editor.commit();

    if(removedLetters.size() > 0) {
      for(RowItem item: mainRowItems) {
        if(removedLetters.contains(item.getLetter()))
          mainRowItems.remove(item);
      }
      mainListAdapter.notifyDataSetChanged();
    }
  }

  public ApplicationInfo getPackageAppInfo(String packageName) {
    ApplicationInfo appInfo = null;
    try {
       appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
    return appInfo;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    TextView v = (TextView) view.findViewById(R.id.main_letter);
    String selectedLetter = v.getText().toString();
    Utils.logInfo(getClass().getName(), "Item Clicked: " + selectedLetter);

    DataModel infoData = shortcutToModelMap.get(selectedLetter);

    if(infoData.isApplicationType()) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(infoData.getActivityCallable());
        startActivity(intent);
    } else if(infoData.isContactType()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + infoData.getActivityCallable()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Utils.logInfo(getClass().getName(), "Item Long Clicked: ");
    DialogBuilder d = new DialogBuilder();
    selectedView = view;
    TextView letterView = (TextView) view.findViewById(R.id.main_letter);
    d.getDialogBuilder(this, R.array.edit_dialog_options, letterView.getText().toString()).show();
    return true;
  }

  public void onMenuAddItemClick() {
    DialogBuilder d = new DialogBuilder();
    d.getDialogBuilder(this, R.array.add_dialog_options, "A").show();
  }

  public void onVShortcutVolCtrlSetting(MenuItem item) {
    String toastString = "VShortcut on Volume Control is ";
    boolean vshortcutVolCtrlFlag;

    if(item.getTitle().equals(getResources().getString(R.string.on_option))) {
      item.setTitle(R.string.off_option);
      vshortcutVolCtrlFlag = true;
      toastString += "Enabled";
    } else {
      item.setTitle(R.string.on_option);
      toastString += "Disabled";
      vshortcutVolCtrlFlag = false;
    }

    Toast t = Toast.makeText(this, toastString, Toast.LENGTH_SHORT);
    t.setGravity(Gravity.CENTER, 0, 0);
    t.show();

    SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putBoolean("VSHORTCUT_VOl_CTRL_ON", vshortcutVolCtrlFlag);
    editor.commit();
  }

  public void showAboutDialog(){
      AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
      TextView tv = new TextView(this);
      tv.setText(R.string.about_us);
      tv.setPadding(20,20,20,20);
      tv.setGravity(Gravity.CENTER_HORIZONTAL);
      Linkify.addLinks(tv, Linkify.WEB_URLS);
      dlgAlert.setTitle("About VShortcut");
      dlgAlert.setView(tv);
      dlgAlert.setPositiveButton("Close", null);
      dlgAlert.setCancelable(true);
      dlgAlert.create().show();
  }
  public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.add_v_shortcut:
        onMenuAddItemClick();
        return true;
      case R.id.on_off_option:
        onVShortcutVolCtrlSetting(item);
        return true;
      case R.id.about:
          showAboutDialog();
          return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void updateUserPreferences(String letter, String showName, String callableName, String type, String imageUri) {
      DataModel dataInfo;
      if(shortcutToModelMap.containsKey(selectedLetter)) {
          dataInfo = shortcutToModelMap.get(letter);
          dataInfo.setShowName(showName);
          dataInfo.setType(type);
          dataInfo.setActivityCallable(callableName);
          dataInfo.setImageUri(imageUri);
      } else {
          dataInfo = new DataModel(letter, showName, type, callableName, imageUri);
          Collections.sort(mainRowItems, new LetterComparator());
          shortcutToModelMap.put(selectedLetter, dataInfo);
      }

      mainListAdapter.notifyDataSetChanged();

      Toast t = Toast.makeText(this, "Shortcut Added", Toast.LENGTH_SHORT);
      t.setGravity(Gravity.CENTER, 0, 0);
      t.show();

      SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPref.edit();
      editor.putString(letter, dataInfo.serialize());
      editor.commit();
  }

  public void onAppsActivityResult(int resultCode, Intent data) {
    if(resultCode == Activity.RESULT_OK) {
      String selectedPackageName = data.getStringExtra("selectedPackage");
      Utils.logInfo(getClass().getName(), "Selected App and shortcut: " + selectedLetter + ", " + selectedPackageName);

        // update the ui list
        ApplicationInfo appInfo = getPackageAppInfo(selectedPackageName);

        if(appInfo != null) {
            String appLoadName = appInfo.loadLabel(getPackageManager()).toString();
            if(shortcutToModelMap.containsKey(selectedLetter)) {
                for(RowItem item: mainRowItems) {
                    if(item.getLetter().equalsIgnoreCase(selectedLetter)) {
                        item.setIcon(appInfo.loadIcon(getPackageManager()));
                        item.setName(appLoadName);
                        break;
                    }
                }
            } else {
                RowItem item = new RowItem(selectedLetter, appLoadName, appInfo.loadIcon(getPackageManager()));
                mainRowItems.add(item);
            }
            updateUserPreferences(selectedLetter, appLoadName, selectedPackageName, "application", null);
        }
    }
  }

  public void onContactActivityResult(int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Uri contactUri = data.getData();
      List<String> projection = new ArrayList<String>();
      projection.add(ContactsContract.CommonDataKinds.Phone.NUMBER);
      projection.add(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);


      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        projection.add(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);

      Cursor cursor = getContentResolver().query(contactUri, projection.toArray(new String[0]), null, null, null);
      cursor.moveToFirst();
      int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
      String number = cursor.getString(column);
      String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
      String uriStr = null;

      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        uriStr = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

      Utils.logInfo(getClass().getName(), "selected letter: " + selectedLetter + ", Selected number: " + number + " Name: "+ givenName + "URI: " + uriStr);
      ImageView iv = new ImageView(this);
      if(uriStr != null)
        iv.setImageURI(Uri.parse(uriStr));
      else
        iv.setImageResource(R.drawable.ic_action_user);

        if(shortcutToModelMap.containsKey(selectedLetter)) {
            DataModel dataInfo = shortcutToModelMap.get(selectedLetter);
            for(RowItem item: mainRowItems) {
                if(item.getLetter().equalsIgnoreCase(selectedLetter)) {
                    item.setIcon(iv.getDrawable());
                    item.setName(givenName);
                    break;
                }
            }
        } else {

            RowItem item = new RowItem(selectedLetter, givenName, iv.getDrawable());
            mainRowItems.add(item);
        }
        updateUserPreferences(selectedLetter, givenName, number, "contact", uriStr);
    }
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // an app is configured
    if(requestCode == 0) {
      onAppsActivityResult(resultCode, data);
    }
    // a number is configured
    else if (requestCode == 1) {
      onContactActivityResult(resultCode, data);
    }
  }

  public void delShortcut() {
    if(selectedView == null)
      return;

    TextView v = (TextView) selectedView.findViewById(R.id.main_letter);
    String selectedLetter = v.getText().toString();

    for(RowItem item: mainRowItems) {
      if(item.getLetter().equalsIgnoreCase(selectedLetter)) {
        mainRowItems.remove(item);
        break;
      }
    }
    //reset ui list view
    mainListAdapter.notifyDataSetChanged();

    shortcutToModelMap.remove(selectedLetter);
    Toast t = Toast.makeText(this, "Shortcut Deleted", Toast.LENGTH_SHORT);
    t.setGravity(Gravity.CENTER, 0, 0);
    t.show();

    // remove the package from shared preferences
    SharedPreferences sharedPref = getSharedPreferences("vshortcut", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.remove(selectedLetter);
    editor.commit();
  }
}