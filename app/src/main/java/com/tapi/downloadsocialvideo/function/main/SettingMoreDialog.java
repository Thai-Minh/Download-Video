package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.downloadsocialvideo.R;

public class SettingMoreDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public static final String BUNDLE_QUALITY_DIALOG_X_KEY = "BUNDLE_QUALITY_DIALOG_X_KEY";
    public static final String BUNDLE_QUALITY_DIALOG_Y_KEY = "BUNDLE_QUALITY_DIALOG_Y_KEY";
    private int localX, localY;
    private Context mContext;
    private SwitchCompat swDarkMode;
    private OnSettingMoreDialogListener listener;

    public static SettingMoreDialog newInstance(int x, int y) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_QUALITY_DIALOG_X_KEY, x);
        args.putInt(BUNDLE_QUALITY_DIALOG_Y_KEY, y);
        SettingMoreDialog fragment = new SettingMoreDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDialogFragment(FragmentManager manager, int x, int y,OnSettingMoreDialogListener listener) {
        SettingMoreDialog moreDialog = SettingMoreDialog.newInstance(x, y);
        moreDialog.setListener(listener);
        moreDialog.show(manager, "dialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_menu, container, false);
    }

    public void setListener(OnSettingMoreDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        removeBackGround();
        if (getArguments() != null) {
            localX = getArguments().getInt(BUNDLE_QUALITY_DIALOG_X_KEY, 0);
            localY = getArguments().getInt(BUNDLE_QUALITY_DIALOG_Y_KEY, 0);
        }
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.LEFT);
                WindowManager.LayoutParams params = window.getAttributes();
                params.x = (Utils.getWidthScreen(mContext) + Utils.convertDpToPixel(16f, mContext)) / 2;
                params.y = localY + Utils.convertDpToPixel(15f, mContext);
                window.setAttributes(params);
            }
        }
        initView(view);
    }

    private void initView(View view) {
        view.findViewById(R.id.popup_share_app_tv).setOnClickListener(this);
        view.findViewById(R.id.popup_update_app_tv).setOnClickListener(this);
        view.findViewById(R.id.popup_private_tv).setOnClickListener(this);
        view.findViewById(R.id.popup_rate_app_tv).setOnClickListener(this);
        swDarkMode = view.findViewById(R.id.popup_dark_mode_sw);
        swDarkMode.setOnCheckedChangeListener(this);
        swDarkMode.setChecked(AppPreferences.INSTANCE.getBoolean(PreferencesContains.THEME, true));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void removeBackGround() {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (getDialog().getWindow() != null)
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
        }
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            switch (v.getId()) {
                case R.id.popup_share_app_tv:
                    listener.onShareApp();
                    break;
                case R.id.popup_update_app_tv:
                    listener.onUpdateApp();
                    break;
                case R.id.popup_private_tv:
                    listener.onPrivacyPolicy();
                    break;
                case R.id.popup_rate_app_tv:
                    listener.onRateApp();
                    break;
            }
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isPressed()) {
            if (listener != null) {
                listener.onChangeTheme(isChecked);
                AppPreferences.INSTANCE.putBoolean(PreferencesContains.THEME, isChecked);
                dismiss();
            }
        }
    }

    public interface OnSettingMoreDialogListener {
        void onShareApp();

        void onUpdateApp();

        void onPrivacyPolicy();

        void onRateApp();

        void onChangeTheme(boolean isDark);
    }
}
