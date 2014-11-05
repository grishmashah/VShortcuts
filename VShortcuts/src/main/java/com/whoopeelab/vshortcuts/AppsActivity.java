package com.whoopeelab.vshortcuts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppsActivity extends Activity implements AdapterView.OnItemClickListener{

  List<RowItem> appsRowItems;
  ListView appsListView;
  Map<String, String> loadNameToPackageMap = new HashMap<String, String>();

  public class LoadNameComparator implements Comparator<RowItem> {
    @Override
    public int compare(RowItem o1, RowItem o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.apps);

    PackageManager pm = getPackageManager();
    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    appsRowItems = new ArrayList<RowItem>();

    String appLoadName;

    for (ApplicationInfo packageInfo : packages) {

      // if the package doesn't have a launchable intent then skip it
      if(pm.getLaunchIntentForPackage(packageInfo.packageName) == null)
        continue;

      appLoadName = packageInfo.loadLabel(pm).toString();
      RowItem item = new RowItem(null, appLoadName, packageInfo.loadIcon(pm));
      Utils.logInfo(getClass().getName(), "item = " + item.toString());
      loadNameToPackageMap.put(appLoadName, packageInfo.packageName);
      appsRowItems.add(item);
    }
    appsListView = (ListView) findViewById(R.id.apps_list);
    // sort the data for list view by apps's loadName
    Collections.sort(appsRowItems, new LoadNameComparator());
    CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.apps_list_item, appsRowItems);
    appsListView.setAdapter(adapter);
    appsListView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Utils.logInfo(getClass().getName(), "Item Clicked: ");
    TextView v = (TextView) view.findViewById(R.id.apps_name);
    String selectedAppLoadName = v.getText().toString();
    Intent resultData = new Intent();
    resultData.putExtra("selectedPackage", loadNameToPackageMap.get(selectedAppLoadName));
    setResult(Activity.RESULT_OK, resultData);
    finish();
  }
}