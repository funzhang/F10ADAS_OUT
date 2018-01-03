package com.neusoft.oddc.activity;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.R;
import com.neusoft.oddc.ui.setting.EntitySettingGroup;
import com.neusoft.oddc.ui.setting.SettingRecyclerAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.listeners.OnGroupClickListener;
import com.neusoft.oddc.widget.recycler.DefaultDividerDecoration;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SettingActivity.class.getSimpleName();

    private List<EntitySettingGroup> entities;

    private OnGroupClickListener onGroupClickListener = new OnGroupClickListener() {
        @Override
        public boolean onGroupClick(int flatPos) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "OnGroupClickListener position = " + flatPos);
            }

            EntitySettingGroup entity = entities.get(flatPos);
            if (null == entity) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "onGroupClick null == entity");
                }
                return false;
            }
            Class clazz = entity.getClazz();
            if (null == clazz) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "onGroupClick null == clazz");
                }
                return false;
            }
            Intent intent = new Intent(SettingActivity.this, clazz);
            startActivity(intent);
            return false;

        }
    };

    static Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mContext = getApplicationContext();
        String tVer = "";
        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            tVer = pinfo.versionName;
        }
        catch (NameNotFoundException e){}

        //setCustomTitle(R.string.title_setting);
        TextView textView = (TextView) findViewById(R.id.custom_title_textview);
        if (null != textView) {
            textView.setText(getString(R.string.title_setting) + "  " + tVer + "  ");
            textView.setGravity(0x15);
        }

        initViews();
        initBackButton();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_title_left_button:
                finish();
                break;
            default:
                break;
        }
    }

    private void initViews() {
        entities = getSettingGroup();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.setting_expandable_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        SettingRecyclerAdapter adapter = new SettingRecyclerAdapter(entities, this);
        adapter.setOnGroupClickListener(onGroupClickListener);
        recyclerView.addItemDecoration(new DefaultDividerDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initBackButton()
    {
        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.back);
            button.setOnClickListener(this);
        }
    }

    private List<EntitySettingGroup> getSettingGroup() {
        List<EntitySettingGroup> settingGroup = new ArrayList<>();
        List<String> childList = new ArrayList<>();
        // childList.add("sub-item1");
        // childList.add("sub-item2");
        // childList.add("sub-item3");
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item1), SettingUserProfileActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item2), SettingVehicleProfileActivity.class, childList));
        // settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item3), SettingAdasCalibrationActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item4), SettingAdasParametersActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item5), SettingDvrSettingActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item6), SettingVinOptionsActivity.class, childList));
        return settingGroup;
    }
}
