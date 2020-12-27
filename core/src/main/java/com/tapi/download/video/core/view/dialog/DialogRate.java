package com.tapi.download.video.core.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tapi.download.video.core.R;

import java.util.ArrayList;

public class DialogRate extends Dialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private IDialogRateListener listener;
    private CheckBox cbRubbish, cbBad, cbOkay, cbGood, cbGreat;
    private TextView txtRatingNow, txtNoRating;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    public DialogRate(@NonNull Context context, IDialogRateListener listener) {
        super(context);
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_rate);
        initViews();

        setData();
    }

    public static void showDialogRate(Context context, IDialogRateListener listener) {
        DialogRate dialogRate = new DialogRate(context, listener);
        dialogRate.show();
    }

    private void initViews() {
        cbRubbish = findViewById(R.id.cb_rubbish);
        cbRubbish.setOnCheckedChangeListener(this);
        cbBad = findViewById(R.id.cb_bad);
        cbBad.setOnCheckedChangeListener(this);
        cbOkay = findViewById(R.id.cb_okay);
        cbOkay.setOnCheckedChangeListener(this);
        cbGood = findViewById(R.id.cb_good);
        cbGood.setOnCheckedChangeListener(this);
        cbGreat = findViewById(R.id.cb_great);
        cbGreat.setOnCheckedChangeListener(this);
        txtRatingNow = findViewById(R.id.txt_rating_now);
        txtRatingNow.setOnClickListener(this);
        txtNoRating = findViewById(R.id.txt_no_rating);
        txtNoRating.setOnClickListener(this);

        //show();
        int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90);
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void setData() {
        checkBoxes.add(cbRubbish);
        checkBoxes.add(cbBad);
        checkBoxes.add(cbOkay);
        checkBoxes.add(cbGood);
        checkBoxes.add(cbGreat);

        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setChecked(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        if (!isChecked) {
            cbRubbish.setChecked(id == R.id.cb_rubbish);
            cbBad.setChecked(!(id == R.id.cb_rubbish || id == R.id.cb_bad));
            cbOkay.setChecked(!(id == R.id.cb_rubbish || id == R.id.cb_bad || id == R.id.cb_okay));
            cbGood.setChecked(!(id == R.id.cb_rubbish || id == R.id.cb_bad || id == R.id.cb_okay || id == R.id.cb_good));
            cbGreat.setChecked(!(id == R.id.cb_rubbish || id == R.id.cb_bad || id == R.id.cb_okay || id == R.id.cb_good || id == R.id.cb_great));
        } else {
            cbRubbish.setChecked(id == R.id.cb_rubbish || id == R.id.cb_bad || id == R.id.cb_okay || id == R.id.cb_good || id == R.id.cb_great);
            cbBad.setChecked(id == R.id.cb_bad || id == R.id.cb_okay || id == R.id.cb_good || id == R.id.cb_great);
            cbOkay.setChecked(id == R.id.cb_okay || id == R.id.cb_good || id == R.id.cb_great);
            cbGood.setChecked(id == R.id.cb_good || id == R.id.cb_great);
            cbGreat.setChecked(id == R.id.cb_great);
        }
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            if (v.getId() == R.id.txt_rating_now) {
                if (cbRubbish.isChecked()) {

                } else if (cbBad.isChecked()) {

                } else if (cbOkay.isChecked()) {

                } else if (cbGood.isChecked()) {

                } else if (cbGreat.isChecked()) {

                }
                listener.showDialogRate();
            } else if (v.getId() == R.id.txt_no_rating) {
            }
            dismiss();
        }
    }
}
