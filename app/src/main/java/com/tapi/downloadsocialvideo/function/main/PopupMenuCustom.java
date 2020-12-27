package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.tapi.downloadsocialvideo.R;

public class PopupMenuCustom extends PopupWindow {
    public PopupMenuCustom(Context context) {
        this(context, null);
    }

    public PopupMenuCustom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupMenuCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public static void showPopup(View view, Context context) {
        PopupMenuCustom popupMenuCustom = new PopupMenuCustom(context);
        popupMenuCustom.showAsDropDown(view);
    }

    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View popupView = layoutInflater.inflate(R.layout.popup_menu, null);
        LinearLayout linearLayout = popupView.findViewById(R.id.popup_group_layout_ll);
        linearLayout.getLayoutParams().width = 10;
        setContentView(popupView);
    }
}
