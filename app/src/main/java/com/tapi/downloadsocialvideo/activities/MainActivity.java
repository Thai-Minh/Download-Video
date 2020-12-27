package com.tapi.downloadsocialvideo.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tapi.download.video.core.BaseActivity;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.core.view.QualityBottomSheetFragment;
import com.tapi.download.video.core.view.dialog.DialogRate;
import com.tapi.download.video.core.view.dialog.IDialogRateListener;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.function.downloader.DownloadManagerImpl;
import com.tapi.downloadsocialvideo.function.downloader.OnDownloadListener;
import com.tapi.downloadsocialvideo.function.downloader.util.DownloaderUtils;
import com.tapi.downloadsocialvideo.function.main.SettingMoreDialog;
import com.tapi.downloadsocialvideo.function.main.SocialManager;
import com.tapi.downloadsocialvideo.function.main.adapter.MainPageAdapter;
import com.tapi.downloadsocialvideo.service.DownloadService;

import java.util.ArrayList;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        OnCatchVideoListener, OnWebFragmentListener, View.OnClickListener, SettingMoreDialog.OnSettingMoreDialogListener,
        IDialogRateListener, OnDownloadListener {

    private static final String TAG = "MainActivity";
    private int[] BIND_CLICK = {R.id.main_more_iv, R.id.layout_no_internet_try_again_bt};
    private ViewPager mViewPager;
    private ImageView ivMore;
    private TextView tvTitle;
    private BottomNavigationView mBottomNavigation;
    private LinearLayout llNInternet;

    private int id = -1;
    private boolean boundService, isNetWork, isBrowser;

    private Task downloadCurrentTask;
    private MainPageAdapter mMainPagerAdapter;
    private BroadcastReceiver broadcastReceiver;
    private DownloadManagerImpl downloadManager;
    private OnSendIntentListener onSendIntentListener;
    private OnCatchVideoListener listener;

    private ArrayList<OnDownloadListener> mListCallBack = new ArrayList<>();
    private ArrayList<Task> mListTask = new ArrayList<>();

    private Intent intentSend;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof DownloadService.DownloadServiceBinder) {
                downloadManager = ((DownloadService.DownloadServiceBinder) service).getService().getDownloadManagerImpl();
                downloadManager.addOnDownloadListener(MainActivity.this);
                for (OnDownloadListener listener : mListCallBack) {
                    downloadManager.addOnDownloadListener(listener);
                }
                mListCallBack.clear();
            }
            boundService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = false;
            downloadManager = null;
        }
    };
    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                isNetWork = com.tapi.downloadsocialvideo.util.Utils.isOnline(context);
                if (isBrowser) {
                    changeLayoutNetwork(isNetWork);
                    sendBroadcastAction(MainActivity.this, Utils.NO_INTERNET);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void findViewById() {
        mBottomNavigation = findViewById(R.id.main_bottom_navigation);
        mViewPager = findViewById(R.id.main_view_pager);
        ivMore = findViewById(R.id.main_more_iv);
        tvTitle = findViewById(R.id.main_title_tv);
        llNInternet = findViewById(R.id.main_layout_include);
    }

    private void initView() {
        tvTitle.setText(SocialManager.getInstance().getTitleApplication(this));
    }

    @Override
    protected void onCreateInit(@Nullable Bundle savedInstanceState) {
        super.onCreateInit(savedInstanceState);
        Log.e(TAG, "onCreateInit: ");
        DownloaderUtils.startService(this);
        DownloaderUtils.bindService(this, serviceConnection);
        SocialManager.getInstance().setCatchLinkListener(this);
        initView();
        initViewPager();
        initNavigation();
        listenerBottomSheetBroadcast();
        registerNetworkChangeReceiver();
        setClick();

        // check twitter initialized
        if (SocialManager.getInstance().getmState() == SocialManager.StateApp.TWITTER && !Fabric.isInitialized()) {
            Utils.checkInitialized(this);
        }

    }

    private void registerNetworkChangeReceiver() {
        try {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkChangeReceiver, filter);
        } catch (Exception e) {
            Log.d("registerNetworkChange", "registerNetworkChangeReceiver: " + e.getMessage());
        }
    }

    private void setClick() {
        bindClicks(this, BIND_CLICK);
    }

    private void initViewPager() {
        mMainPagerAdapter = new MainPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 2:
                        isBrowser = true;
                        mBottomNavigation.setSelectedItemId(R.id.navigation_browser);
                        changeLayoutNetwork(isNetWork);
                        break;
                    case 1:
                        isBrowser = false;
                        mBottomNavigation.setSelectedItemId(R.id.navigation_download);
                        changeLayoutNetwork(true);
                        break;
                    case 3:
                        isBrowser = false;
                        mBottomNavigation.setSelectedItemId(R.id.navigation_market);
                        changeLayoutNetwork(true);
                        break;
                    default:
                        isBrowser = false;
                        mBottomNavigation.setSelectedItemId(R.id.navigation_home);
                        changeLayoutNetwork(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (onSendIntentListener != null) {
            onSendIntentListener.onSendIntent(intent);
            mViewPager.setCurrentItem(0);
        }
        this.intentSend = intent;
    }

    public Intent getIntentSend() {
        return intentSend;
    }

    private void changeLayoutNetwork(boolean isNetWork) {
        llNInternet.setVisibility(isNetWork ? View.GONE : View.VISIBLE);
    }

    private void initNavigation() {
        mBottomNavigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_browser:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.navigation_download:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.navigation_market:
                mViewPager.setCurrentItem(3);
                break;
            default:
                mViewPager.setCurrentItem(0);
                break;
        }
        return true;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof OnDownloadListener) {
            OnDownloadListener callBack = (OnDownloadListener) fragment;
            if (downloadManager != null) {
                downloadManager.addOnDownloadListener(callBack);
            } else {
                mListCallBack.add(callBack);
            }
        }
        if (fragment instanceof IWebFragment) {
            IWebFragment callBack = (IWebFragment) fragment;
            callBack.setWebFragmentListener(this);
        }
    }

    @Override
    public void onDelayBottomSheet() {

    }

    @Override
    public void onListDownloadChanged(Video video) {
        if (video != null) {
            if (!checkVideoDownloaded(video)) {
                QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), video);
            }
        }
    }

    private boolean checkVideoDownloaded(Video video) {
        for (Task task : mListTask) {
            if (task.idServer.equalsIgnoreCase(video.getIdVideo())) {
                QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), video, task);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDownloadTaskListChange(ArrayList<Task> downloadTasks) {
        this.mListTask = downloadTasks;
        for (Task task : downloadTasks) {
            if (task.id == id) {
                this.downloadCurrentTask = task;
            }
        }
    }

    public ArrayList<Task> getmListTask() {
        return mListTask;
    }

    @Override
    public void onDownloadStateChange(Task downloadTask) {
        updateDownloadItemChange(downloadTask);
    }

    @Override
    public void onDownloadProgressChange(Task downloadTask) {
        this.downloadCurrentTask = downloadTask;

        updateDownloadItemChange(downloadTask);

        if (downloadTask.state == TaskStates.END) {
            sendBroadcastAction(this, Utils.CALLBACK_ACTION_DOWNLOAD_END);
        }
    }

    @Override
    public void onStartCatch() {
        if (listener != null)
            listener.onStartCatch();

    }

    @Override
    public void onCatchedLink(Video video) {
        if (listener != null)
            listener.onCatchedLink(video);
    }

    @Override
    public void onPrivateLink(String link) {
        if (listener != null)
            listener.onPrivateLink(link);
        mViewPager.setCurrentItem(2);
        sendPrivateLink(link);
    }

    private void sendPrivateLink(String linkPrivate) {
        Intent intent = new Intent(Utils.ACTION_PRIVATE_LINK);
        intent.putExtra(Utils.PRIVATE_LINK, linkPrivate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocialManager.getInstance().releaseInstance();
        downloadManager.onClearAllListener();
        if (boundService) {
            unbindService(serviceConnection);
            boundService = false;
        }
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    public void setListener(OnCatchVideoListener listener) {
        this.listener = listener;
    }

    public int startDownload(Video video, DownloadLink link) {
        if (downloadManager != null)
            return downloadManager.startDownload(video, link);
        return -1;
    }

    public void cancelDownload(long downloadId) {
        if (downloadManager != null)
            downloadManager.cancelDownload(downloadId);
    }

    public void deleteDownload(long downloadId) {
        id = -1;
        if (downloadManager != null) {
            downloadManager.deleteDownload(downloadId);
        }
    }

    public void pauseDownload(long downloadId) {
        if (downloadManager != null) {
            downloadManager.pauseDownload(downloadId);
        }
    }

    public void resumeDownload(long downloadId) {
        if (downloadManager != null) {
            downloadManager.resumeDownload(downloadId);
        }
    }

    private void updateDownloadItemChange(Task downloadTask) {
        if (downloadTask.id == id) {
            Log.e(TAG, "updateDownloadItemChange: " );
            sendBroadcastStatusDownload(this, downloadTask, Utils.CALLBACK_ACTION_DOWNLOAD_STATE);
        }
    }

    private void listenerBottomSheetBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.ACTION_RESUME_DOWNLOAD);
        intentFilter.addAction(Utils.ACTION_SHOW_BOTTOM_SHEET);
        intentFilter.addAction(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_START);
        intentFilter.addAction(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_CANCEL);
        intentFilter.addAction(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_PAUSE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int downloadState = 0, position;
                Video video;

                if (action.equals(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_START)) {
                    video = (Video) intent.getSerializableExtra(Utils.BOTTOM_SHEET_VIDEO);
                    position = intent.getIntExtra(Utils.BOTTOM_SHEET_LINK_POSITION, 0);
                    downloadVideo(video, position);
                } else if (action.equals(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_CANCEL)) {
                    deleteDownload(downloadCurrentTask.id);
                } else if (action.equals(Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_PAUSE)) {
                    if (downloadCurrentTask != null) {
                        downloadState = downloadCurrentTask.state;
                        if (downloadState == TaskStates.PAUSED) {
                            Log.e(TAG, "onReceive: PAUSED" );
                            resumeDownload(downloadCurrentTask.id);
                            sendBroadcastStatusDownload(context, downloadCurrentTask, Utils.CALLBACK_ACTION_DOWNLOAD_STATE);
                            sendBroadcastStatusDownload(context, downloadCurrentTask, Utils.CALLBACK_ACTION_DOWNLOAD_DOWNLOADING);
                        } else if (downloadState == TaskStates.DOWNLOADING) {
                            pauseDownload(downloadCurrentTask.id);
                            sendBroadcastStatusDownload(context, downloadCurrentTask, Utils.CALLBACK_ACTION_DOWNLOAD_PAUSE);
                        }
                    }
                } else if (action.equals(Utils.ACTION_SHOW_BOTTOM_SHEET)) {
                    sendBroadcastActionCheckId(context, mListTask);
                } else if (action.equals(Utils.ACTION_RESUME_DOWNLOAD)) {
                    downloadCurrentTask = (Task) intent.getSerializableExtra(Utils.INTENT_ACTION_RESUME_DOWNLOAD);
                    id = downloadCurrentTask.id;

                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void sendBroadcastAction(Context context, String actionName) {
        Intent intent = new Intent(actionName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadcastActionCheckId(Context context, ArrayList<Task> listTask) {
        Intent intent = new Intent(Utils.ACTION_CHECK_ID);
        intent.putExtra(Utils.INTENT_ACTION_CHECK_ID, listTask);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadcastStatusDownload(Context context, Task downloadTask, String actionName) {
        Intent intent = new Intent(actionName);
        intent.putExtra(Utils.BOTTOM_SHEET_PERCENT, downloadTask);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void downloadVideo(Video videoDownload, int position) {
        if (videoDownload != null) {
            DownloadLink downloadLink = videoDownload.getLinks().get(position);

            if (downloadManager != null) {
                id = downloadManager.startDownload(videoDownload, downloadLink);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_more_iv:
                SettingMoreDialog.showDialogFragment(getSupportFragmentManager(), (int) ivMore.getX(), (int) ivMore.getY(), this);
                break;
            case R.id.layout_no_internet_try_again_bt:
                if (!isNetWork) {
                    Toast.makeText(this, R.string.fb_home_check_internet, Toast.LENGTH_SHORT).show();
                } else {
                    changeLayoutNetwork(true);
                    sendBroadcastAction(MainActivity.this, Utils.NO_INTERNET);
                }
                break;
        }
    }

    @Override
    public void onShareApp() {
        com.tapi.downloadsocialvideo.util.Utils.shareApp(this);
    }

    @Override
    public void onUpdateApp() {

    }

    @Override
    public void onPrivacyPolicy() {
        com.tapi.downloadsocialvideo.util.Utils.showLinkWebView(this, com.tapi.downloadsocialvideo.util.Utils.URL_PRIVACY_POLICY);
    }

    @Override
    public void onRateApp() {
        DialogRate.showDialogRate(this, this);
    }

    @Override
    public void onChangeTheme(boolean isDark) {

    }

    @Override
    public void showDialogRate() {
        com.tapi.downloadsocialvideo.util.Utils.showLinkWebView(this, String.format(Locale.ENGLISH, com.tapi.downloadsocialvideo.util.Utils.LINK_APP_STORE, getPackageName()));
    }

    @Override
    public boolean doBackPressed() {
        if (mViewPager.getCurrentItem() != 0) {
            if (mViewPager.getCurrentItem() == 2) {
                return false;
            } else {
                mViewPager.setCurrentItem(0);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean fragmentNoBack() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
            return true;
        }
        return false;
    }

    public void setOnSendIntentListener(OnSendIntentListener onSendIntentListener) {
        this.onSendIntentListener = onSendIntentListener;
    }

    public interface OnSendIntentListener {
        void onSendIntent(Intent intent);
    }
}
