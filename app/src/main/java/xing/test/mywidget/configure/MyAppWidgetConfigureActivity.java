package xing.test.mywidget.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xing.test.mywidget.BuildConfig;
import xing.test.mywidget.Utils;
import xing.test.mywidget.appwidget.AppInfo;
import xing.test.mywidget.appwidget.MyAppWidget;
import xing.test.mywidget.R;

/**
 * The configuration screen for the {@link MyAppWidget MyAppWidget} AppWidget.
 */
public class MyAppWidgetConfigureActivity extends Activity {
    private static final String TAG = "MyAppWidgetConfigureAct";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MyAppWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            Utils.savePackageNameListPref(context, mAppWidgetId, mAdapter.getSelectedPackageName());
//            Utils.updateStateListPref(context, mAppEnableStateMap);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            MyAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    private boolean mIncludeSystemApp = false;
    private boolean mIncludeUserApp = true;

    private List<AppInfo> mAppInfoList = new ArrayList<>();
    private HashMap<String, Boolean> mAppEnableStateMap = new HashMap<>();

    private CheckBox mCbSystemApp;
    private CheckBox mCbUserApp;
    private RecyclerView mRvAppList;
    private AppListAdapter mAdapter;

    public MyAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.my_app_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        initView();
        initData();

    }

    private void updateAppList() {
        getPackageList(this, mIncludeSystemApp, mIncludeUserApp);
    }

    private void initData() {
        updateAppList();
        mAdapter = new AppListAdapter(this, mAppInfoList);
        mRvAppList.setAdapter(mAdapter);
    }

    private void initView() {
        findViews();
        mCbSystemApp.setChecked(mIncludeSystemApp);
        mCbUserApp.setChecked(mIncludeUserApp);
        mRvAppList.setLayoutManager(new GridLayoutManager(this, 4));

        mCbSystemApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIncludeSystemApp = isChecked;
                updateAppList();
                mAdapter.notifyDataSetChanged();
            }
        });
        mCbUserApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIncludeUserApp = isChecked;
                updateAppList();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void findViews() {
        mCbSystemApp = findViewById(R.id.cb_system);
        mCbUserApp = findViewById(R.id.cb_user);
        mRvAppList = findViewById(R.id.rv_app_list);

        findViewById(R.id.btn_select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectAll();
            }
        });
        findViewById(R.id.btn_un_select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.unSelectAll();
            }
        });
    }

    private void getPackageList(Context context, boolean includeSystemApp, boolean includeUserApp) {
        mAppInfoList.clear();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        int index = 0;
        for (PackageInfo packageInfo : packageInfoList) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (BuildConfig.APPLICATION_ID.equals(applicationInfo.packageName)) {
                continue;
            }
            boolean isSystemApp = Utils.isSystemApp(packageInfo);
            if ((includeSystemApp && isSystemApp) || (includeUserApp && !isSystemApp)) {
                String appName = pm.getApplicationLabel(applicationInfo).toString();
                String packageName = packageInfo.packageName;
                Drawable appIcon = pm.getApplicationIcon(applicationInfo);
                AppInfo appInfo = new AppInfo(appName, packageName, appIcon);
                appInfo.enabled = applicationInfo.enabled;

                mAppInfoList.add(appInfo);
                mAppEnableStateMap.put(packageName, appInfo.enabled);
                index++;
            }

        }
    }
}

