package com.whoopeelab.vshortcuts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

  Context context;
  int resourceId;
  public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
    super(context, resourceId, items);
    this.context = context;
    this.resourceId = resourceId;
  }

  /* private view holder class for main_list and apps_list */
  private class MainListViewHolder {
    TextView txtLetter;
    ImageView imageView;
    TextView txtName;
  }
  private class AppsListViewHolder {
    ImageView imageView;
    TextView txtName;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    MainListViewHolder mainHolder = null;
    AppsListViewHolder appsHolder = null;

    RowItem rowItem = getItem(position);

    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if (convertView == null) {
      if(resourceId == R.layout.main_list_item) {
        convertView = mInflater.inflate(R.layout.main_list_item, null);
        mainHolder = new MainListViewHolder();
        mainHolder.imageView = (ImageView) convertView.findViewById(R.id.main_icon);
        mainHolder.txtLetter = (TextView) convertView.findViewById(R.id.main_letter);
        mainHolder.txtName = (TextView) convertView.findViewById(R.id.main_name);
        convertView.setTag(mainHolder);
      } else {
        convertView = mInflater.inflate(R.layout.apps_list_item, null);
        appsHolder = new AppsListViewHolder();
        appsHolder.imageView = (ImageView) convertView.findViewById(R.id.apps_icon);
        appsHolder.txtName = (TextView) convertView.findViewById(R.id.apps_name);
        convertView.setTag(appsHolder);
      }
    } else {
      if(resourceId == R.layout.main_list_item) {
        mainHolder = (MainListViewHolder) convertView.getTag();
      } else {
        appsHolder = (AppsListViewHolder) convertView.getTag();
      }
    }

    if(resourceId == R.layout.main_list_item) {
      mainHolder.txtLetter.setText(rowItem.getLetter());
      mainHolder.txtName.setText(rowItem.getName());
      mainHolder.imageView.setImageDrawable(rowItem.getIcon());
    } else {
      appsHolder.txtName.setText(rowItem.getName());
      appsHolder.imageView.setImageDrawable(rowItem.getIcon());
    }

    return convertView;
  }
}