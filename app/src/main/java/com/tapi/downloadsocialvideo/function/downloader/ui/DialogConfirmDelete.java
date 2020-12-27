package com.tapi.downloadsocialvideo.function.downloader.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

import com.tapi.downloadsocialvideo.R;

public class DialogConfirmDelete extends Dialog implements View.OnClickListener {
    private OnDialogConFirm onDialogConFirm;
    private long mDownloadId;

    public DialogConfirmDelete(@NonNull Context context, long downloadId) {
        super(context);
        this.mDownloadId = downloadId;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null)
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_confirm_delete);
        initView();

    }

    public static void showDialogConfirm(Context context, OnDialogConFirm onDialogConFirm, long downloadId) {
        DialogConfirmDelete confirmDelete = new DialogConfirmDelete(context, downloadId);
        confirmDelete.setOnDialogConFirm(onDialogConFirm);
        confirmDelete.show();
    }

    public void setOnDialogConFirm(OnDialogConFirm onDialogConFirm) {
        this.onDialogConFirm = onDialogConFirm;
    }

    private void initView() {
        findViewById(R.id.dialog_confirm_delete_cancel).setOnClickListener(this);
        findViewById(R.id.dialog_confirm_delete_oke).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_confirm_delete_oke:
                if (onDialogConFirm != null)
                    onDialogConFirm.onConfirmOk(mDownloadId);
                break;
        }
        cancel();
    }

    public interface OnDialogConFirm {
        void onConfirmOk(long mDownloadId);
    }
}
