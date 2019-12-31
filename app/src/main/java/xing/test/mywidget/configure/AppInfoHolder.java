package xing.test.mywidget.configure;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import xing.test.mywidget.R;
import xing.test.mywidget.appwidget.AppInfo;

class AppInfoHolder extends RecyclerView.ViewHolder {

    private ImageView mIvAppIcon;
    private TextView mTvAppName;
    private CheckBox mCbCheck;
    private OnItemCheckedListener mOnItemCheckedListener;

    private AppInfo mAppInfo;

    public AppInfoHolder(@NonNull View itemView) {
        super(itemView);
        mIvAppIcon = itemView.findViewById(R.id.iv_icon);
        mTvAppName = itemView.findViewById(R.id.tv_name);
        mCbCheck = itemView.findViewById(R.id.cb_check);
        mCbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnItemCheckedListener != null) {
                    mOnItemCheckedListener.onCheckedChanged(isChecked, mAppInfo.packageName);
                }
            }
        });
    }

    public void bindView(AppInfo appInfo, boolean isChecked) {
        mAppInfo = appInfo;
        mIvAppIcon.setImageDrawable(appInfo.appIcon);
        mTvAppName.setText(appInfo.appName);
        mCbCheck.setChecked(isChecked);
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        this.mOnItemCheckedListener = listener;
    }

    interface OnItemCheckedListener {
        void onCheckedChanged(boolean isChecked, String packageName);
    }

}
