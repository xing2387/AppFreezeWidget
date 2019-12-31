package xing.test.mywidget.configure;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;

import xing.test.mywidget.R;
import xing.test.mywidget.appwidget.AppInfo;

class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AppInfoHolder.OnItemCheckedListener {

    private List<AppInfo> mAppInfoList;
    private Context mContext;
    private HashSet<String> mSelectedPackageName = new HashSet<>();

    public AppListAdapter(Context context, List<AppInfo> appInfoList) {
        mAppInfoList = appInfoList;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_configure_app, viewGroup, false);
        AppInfoHolder holder = new AppInfoHolder(view);
        holder.setOnItemCheckedListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof AppInfoHolder && mAppInfoList.size() > i) {
            AppInfo appInfo = mAppInfoList.get(i);
            boolean isChecked = mSelectedPackageName.contains(appInfo.packageName);
            ((AppInfoHolder) viewHolder).bindView(appInfo, isChecked);
        }
    }

    @Override
    public int getItemCount() {
        return mAppInfoList == null ? 0 : mAppInfoList.size();
    }

    @Override
    public void onCheckedChanged(boolean isChecked, String packageName) {
        if (isChecked) {
            mSelectedPackageName.add(packageName);
        } else {
            mSelectedPackageName.remove(packageName);
        }
    }

    public HashSet<String> getSelectedPackageName() {
        return mSelectedPackageName;
    }

    public void selectAll() {
        for (AppInfo appInfo : mAppInfoList) {
            mSelectedPackageName.add(appInfo.packageName);
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        mSelectedPackageName.clear();
        notifyDataSetChanged();
    }
}
