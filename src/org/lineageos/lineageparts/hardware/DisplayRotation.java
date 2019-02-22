/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package org.lineageos.lineageparts.hardware;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.view.RotationPolicy;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

public class DisplayRotation extends SettingsPreferenceFragment {
    private static final String TAG = "DisplayRotation";

    public static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String ROTATION_0_PREF = "display_rotation_0";
    private static final String ROTATION_90_PREF = "display_rotation_90";
    private static final String ROTATION_180_PREF = "display_rotation_180";
    private static final String ROTATION_270_PREF = "display_rotation_270";

    private SwitchPreference mAccelerometer;
    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;

    public static final int ROTATION_0_MODE = 1;
    public static final int ROTATION_90_MODE = 2;
    public static final int ROTATION_180_MODE = 4;
    public static final int ROTATION_270_MODE = 8;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.display_rotation);

        PreferenceScreen prefSet = getPreferenceScreen();

        mAccelerometer = (SwitchPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);

        mRotation0Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);

        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                ROTATION_0_MODE | ROTATION_90_MODE | ROTATION_270_MODE);

        mRotation0Pref.setChecked((mode & ROTATION_0_MODE) != 0);
        mRotation90Pref.setChecked((mode & ROTATION_90_MODE) != 0);
        mRotation180Pref.setChecked((mode & ROTATION_180_MODE) != 0);
        mRotation270Pref.setChecked((mode & ROTATION_270_MODE) != 0);

        watch(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION));
    }

    @Override
    public void onSettingsChanged(Uri contentUri) {
        super.onSettingsChanged(contentUri);
        updateAccelerometerRotationSwitch();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        updateAccelerometerRotationSwitch();
    }

    private void updateAccelerometerRotationSwitch() {
        mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
    }

    private int getRotationBitmask() {
        int mode = 0;
        if (mRotation0Pref.isChecked()) {
            mode |= ROTATION_0_MODE;
        }
        if (mRotation90Pref.isChecked()) {
            mode |= ROTATION_90_MODE;
        }
        if (mRotation180Pref.isChecked()) {
            mode |= ROTATION_180_MODE;
        }
        if (mRotation270Pref.isChecked()) {
            mode |= ROTATION_270_MODE;
        }
        return mode;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAccelerometer) {
            RotationPolicy.setRotationLockForAccessibility(getActivity(),
                    !mAccelerometer.isChecked());
        } else if (preference == mRotation0Pref ||
                preference == mRotation90Pref ||
                preference == mRotation180Pref ||
                preference == mRotation270Pref) {
            int mode = getRotationBitmask();
            if (mode == 0) {
                mode |= ROTATION_0_MODE;
                mRotation0Pref.setChecked(true);
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, mode);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    public static final SummaryProvider SUMMARY_PROVIDER = new SummaryProvider() {
        @Override
        public String getSummary(Context context, String key) {
            if (RotationPolicy.isRotationLocked(context)) {
                return context.getString(R.string.display_rotation_disabled);
            }
            return context.getString(R.string.display_rotation_enabled);
        }
    };
}
