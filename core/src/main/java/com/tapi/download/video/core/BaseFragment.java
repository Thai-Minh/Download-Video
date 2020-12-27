package com.tapi.download.video.core;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    public boolean onBackPressed() {
        return false;
    }

    protected boolean onBackStack() {
        if (getActivity() != null) {
            if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() != 0) {
                getFragmentManager().popBackStack();
                return true;
            }
        }
        return false;
    }
}
