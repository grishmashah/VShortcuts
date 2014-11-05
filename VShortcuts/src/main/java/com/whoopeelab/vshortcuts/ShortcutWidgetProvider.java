package com.whoopeelab.vshortcuts;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ShortcutWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {

            int appWidgetId = appWidgetIds[i];

            Intent actIntent = new Intent(context, ShortcutVoiceActivity.class);
            actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, actIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            // call android speech recognizer activity on widget image or text click
            views.setOnClickPendingIntent(R.id.shortcut_widget_image, pendingIntent);
            views.setOnClickPendingIntent(R.id.shortcut_widget_text, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }
}