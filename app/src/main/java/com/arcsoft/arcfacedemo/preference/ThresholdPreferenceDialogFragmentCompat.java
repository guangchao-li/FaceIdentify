/*
 * Copyright 2018 The Android Open Source Project
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

package com.arcsoft.arcfacedemo.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.arcsoft.arcfacedemo.R;


public class ThresholdPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements View.OnClickListener {
    private static final String TAG = "ThresholdPreferenceDial";
    private static final String SAVE_STATE_TEXT = "EditTextPreferenceDialogFragment.text";

    private EditText mEditText;
    private CharSequence mText;

    public static ThresholdPreferenceDialogFragmentCompat newInstance(String key) {
        final ThresholdPreferenceDialogFragmentCompat
                fragment = new ThresholdPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mText = getThresholdPreference().getText();
        } else {
            mText = savedInstanceState.getCharSequence(SAVE_STATE_TEXT);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TEXT, mText);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.pref_threshold_setting, null, false);
    }
    @Override
    protected void onBindDialogView(View prefThresholdSetting) {
        super.onBindDialogView(prefThresholdSetting);
        ImageView mIvIncrease = prefThresholdSetting.findViewById(R.id.iv_increase);
        ImageView mIvDecrease = prefThresholdSetting.findViewById(R.id.iv_decrease);
        mIvIncrease.setOnClickListener(this);
        mIvDecrease.setOnClickListener(this);

        mEditText = prefThresholdSetting.findViewById(R.id.et_threshold);

        mEditText.requestFocus();
        mEditText.setText(mText);
        // Place cursor at the end
        mEditText.setSelection(mEditText.getText().length());
        if (getThresholdPreference().getOnBindEditTextListener() != null) {
            getThresholdPreference().getOnBindEditTextListener().onBindEditText(mEditText);
        }
    }

    private ThresholdPreference getThresholdPreference() {
        return (ThresholdPreference) getPreference();
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (!isContentValid(value)) {
                Toast.makeText(getContext(), R.string.threshold_value_illegal, Toast.LENGTH_SHORT).show();
                return;
            }
            final ThresholdPreference preference = getThresholdPreference();
            if (preference.callChangeListener(value)) {
                preference.setText(value);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_increase:
                increase();
                break;
            case R.id.iv_decrease:
                decrease();
                break;
            default:
                break;
        }
    }

    private boolean isContentValid(float number) {
        return (number >= 0 && number <= 1.0);
    }

    private boolean isContentValid(String number) {
        float threshold = 0;
        try {
            threshold = Float.parseFloat(number);
            return isContentValid(threshold);
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    private void increase() {
        if (TextUtils.isEmpty(mEditText.getText())) {
            return;
        }
        float threshold = Float.parseFloat(mEditText.getText().toString());
        if (threshold <= 0.99f) {
            threshold += 0.011f;
            mEditText.setText(String.format("%.2f",threshold));
        }
    }

    private void decrease() {
        if (TextUtils.isEmpty(mEditText.getText())) {
            return;
        }
        float threshold = Float.parseFloat(mEditText.getText().toString());
        if (threshold >= 1) {
            threshold = 1;
        }
        if (threshold >= 0.01f) {
            threshold -= 0.009f;
            mEditText.setText(String.format("%.2f",threshold));
        }
    }

}
