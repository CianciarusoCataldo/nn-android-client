package com.cianciaruso_cataldo.cnn.image_analyzer.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.cianciaruso_cataldo.cnn.image_analyzer.R;

@SuppressWarnings("unused")
public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context c){
        super(c);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.getContext().setTheme(R.style.SettingDialogStyle);
        super.onPrepareDialogBuilder(builder);
    }
}