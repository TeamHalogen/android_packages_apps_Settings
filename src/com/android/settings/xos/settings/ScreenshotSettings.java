/*
 * Copyright (C) 2016 halogenOS (XOS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.xos.settings;


import com.android.internal.view.RotationPolicy;
import com.android.settings.DropDownPreference.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.R;

import static android.provider.Settings.System.SCREENSHOT_CROP_AND_SHARE;
import static android.provider.Settings.System.SCREENSHOT_CROP_BEHAVIOR;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String TAG = "ScreenshotSettings";
    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;

    private static final String
		KEY_SCREENSHOT_CROP_AND_SHARE = "screenshot_crop_and_share",
		KEY_SCREENSHOT_CROP_BEHAVIOR = "screenshot_crop_behavior",
                SCREENSHOT_CROP_AND_SHARE = "screenshot_crop_and_share",
                SCREENSHOT_CROP_BEHAVIOR = "screenshot_crop_behavior"
                ;
    
    private final Configuration mCurConfig = new Configuration();
    
    private SwitchPreference 
                mScreenshotCropAndSharePreference,
                mScreenshotCropBehaviorPreference
                ;

    @Override
    protected int getMetricsCategory() {
        return 0;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();

        addPreferencesFromResource(R.xml.screenshot_settings);

        mScreenshotCropAndSharePreference = (SwitchPreference) findPreference(KEY_SCREENSHOT_CROP_AND_SHARE);
            mScreenshotCropAndSharePreference.setOnPreferenceChangeListener(this);

	mScreenshotCropBehaviorPreference = (SwitchPreference) findPreference(KEY_SCREENSHOT_CROP_BEHAVIOR);
            mScreenshotCropBehaviorPreference.setOnPreferenceChangeListener(this);
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                        }
                    });
        }
        return null;
    }

    private void updateState() {
        // Update crop and share if it is available.
        if (mScreenshotCropAndSharePreference != null) {
            int value = Settings.System.getInt(getContentResolver(), SCREENSHOT_CROP_AND_SHARE, 1);
            mScreenshotCropAndSharePreference.setChecked(value != 0);
        }

 	if (mScreenshotCropBehaviorPreference != null) {
            int value = Settings.System.getInt(getContentResolver(), SCREENSHOT_CROP_BEHAVIOR, 1);
            mScreenshotCropBehaviorPreference.setChecked(value != 0);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * Add you preference handlers here.
     * Don't create custom ones.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mScreenshotCropAndSharePreference) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), SCREENSHOT_CROP_AND_SHARE, value ? 1 : 0);
        }
	if (preference == mScreenshotCropBehaviorPreference) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), SCREENSHOT_CROP_BEHAVIOR, value ? 1 : 0);
        }
        
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_display;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.screenshot_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();

                    return result;
                }
            };
}
