package xing.test.mywidget.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.DataOutputStream;
import java.util.List;

import xing.test.mywidget.R;
import xing.test.mywidget.Utils;
import xing.test.mywidget.configure.MyAppWidgetConfigureActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyAppWidgetConfigureActivity MyAppWidgetConfigureActivity}
 */
public class MyAppWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {

        //设置图标Adapter
        Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.my_app_widget);
        rv.setRemoteAdapter(R.id.grid_view, intent);

        //设置点击后发送的PendingIntent
        Intent toastIntent = new Intent(context, MyAppWidget.class);
        toastIntent.setAction(MyAppWidget.SWITCH_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.grid_view, toastPendingIntent);

        Intent editIntent = new Intent(context, MyAppWidget.class);
        editIntent.setAction(MyAppWidget.EDIT_ACTION);
        editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent editPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn_edit_mode, editPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            Utils.deletePackageNameListPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static final String SWITCH_ACTION = "xing.test.mywidget.SWITCH_ACTION";
    public static final String EDIT_ACTION = "xing.test.mywidget.EDIT_ACTION";
    public static final String APP_LIST = "xing.test.mywidget.APP_LIST";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (SWITCH_ACTION.equals(intent.getAction())) {
            String content = intent.getStringExtra(APP_LIST);
            String[] packageNames = content.split(",");
            StringBuilder sbCmd = new StringBuilder();
            PackageManager pm = context.getPackageManager();
            for (String name : packageNames) {
//                boolean isEnable = Utils.getEnableState(context, name);

                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = pm.getApplicationInfo(name, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (applicationInfo != null) {
                    sbCmd.append("pm " + (!applicationInfo.enabled ? "enable " : "disable ") + name + ";");
                    Utils.saveEnableState(context, name, !applicationInfo.enabled);
                }
            }
            rootCommand(sbCmd.toString());
//            Toast.makeText(context, "Touched view " + content, Toast.LENGTH_SHORT).show();

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view);
        } else if (EDIT_ACTION.equals(intent.getAction())) {
            boolean isEditMode = Utils.loadEditModePref(context, appWidgetId);
            Utils.setEditModePref(context, appWidgetId, !isEditMode);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view);
        }
        super.onReceive(context, intent);
    }

    public static boolean rootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        Log.d("*** DEBUG ***", "Root SUC ");
        return true;
    }
}

