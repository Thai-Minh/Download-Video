package com.tapi.downloadsocialvideo.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.tapi.download.video.core.BaseActivity;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.downloadsocialvideo.BuildConfig;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.function.main.SocialManager;
import com.tapi.downloadsocialvideo.util.Utils;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 11;
    private static final int[] BIND_CLICK = {R.id.agree_start_iv};
    private TextView tvPrivacyPolicy, tvVersion,tvNameApp;

    private Intent intent;

    public static boolean checkReadWritePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void findViewById() {
        tvPrivacyPolicy = findViewById(R.id.agree_privacy_policy_tv);
        tvVersion = findViewById(R.id.agree_version_tv);
        tvNameApp = findViewById(R.id.welcome_name_app_tv);
    }

    @Override
    protected void onCreateInit(@Nullable Bundle savedInstanceState) {
        super.onCreateInit(savedInstanceState);
        if (checkReadWritePermission(this) && AppPreferences.INSTANCE.getBoolean(PreferencesContains.FIRST_APP, false)) {
            if (intent == null) {
                intent = getIntent();
                if (intent == null) {
                    intent = new Intent();
                }
                intent.setClass(this, MainActivity.class);
                startIntent(intent);
            } else {
                intent.setClass(this, MainActivity.class);
                startIntent(intent);
            }
        } else {
            initPrivacyTextView();
            bindClicks(this, BIND_CLICK);
            tvVersion.setText(getString(R.string.welcome_version, BuildConfig.VERSION_NAME));
            tvNameApp.setText(SocialManager.getInstance().getNameApp(this));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.intent = intent;
    }

    private void initPrivacyTextView() {
        String privacy = getString(R.string.welcome_privacy_policy);
        String term = getString(R.string.welcome_terms_and_service);
        String agreeText = getString(R.string.welcome_agree_text, privacy, term);

        SpannableString ss = new SpannableString(agreeText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Utils.showLinkWebView(WelcomeActivity.this, Utils.URL_PRIVACY_POLICY);
            }
        };
        ClickableSpan privacyClick = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Utils.showLinkWebView(WelcomeActivity.this, Utils.URL_PRIVACY_POLICY);
            }
        };

        int positionPrivacy = agreeText.indexOf(privacy);
        int endPrivacy = positionPrivacy + privacy.length();
        int lengthAgreeText = agreeText.length();
        ss.setSpan(new StyleSpan(Typeface.BOLD), positionPrivacy, endPrivacy, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        int startTeam = agreeText.indexOf(term);
        ss.setSpan(new StyleSpan(Typeface.BOLD), startTeam, lengthAgreeText, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(privacyClick, positionPrivacy, endPrivacy, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(clickableSpan, startTeam, lengthAgreeText, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        tvPrivacyPolicy.setText(ss, TextView.BufferType.SPANNABLE);
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivacyPolicy.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == -1) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, R.string.welcome_need_permission, Toast.LENGTH_SHORT).show();
                    } else {
                        dialogEnabelPermission(this);
                    }
                } else {
                    if (intent == null) {
                        intent = new Intent();
                    }
                    intent.setClass(this, MainActivity.class);
                    AppPreferences.INSTANCE.putBoolean(PreferencesContains.FIRST_APP, true);
                    startIntent(intent);
                }
            }
        }
    }

    private void startIntent(Intent intent) {
        startActivity(intent);
        finish();
    }

    public void dialogEnabelPermission(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.dialog_permission));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirm_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.openPermissionSetting(context);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.confirm_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}