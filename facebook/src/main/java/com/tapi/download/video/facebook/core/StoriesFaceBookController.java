package com.tapi.download.video.facebook.core;

import android.util.Log;

import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class StoriesFaceBookController {
    public static final String LOADING = "LOADING";
    private static final String TAG = "LoadMoreDataFaceBook";
    private static StoriesFaceBookController instance;
    private final ArrayList<OnStoriesListener> mListListener = new ArrayList<>();
    private ArrayList<Stories> mListStories = new ArrayList<>();
    private int posStorySelect;
    private volatile boolean isLoadMoreRunning, isNoDataLoadMore;
    private boolean isFunLoadRunning;

    public static StoriesFaceBookController getInstance() {
        if (instance == null) {
            instance = new StoriesFaceBookController();
        }
        return instance;
    }

    public void clearAllListData(){
        mListStories.clear();
    }

    public int getPosStorySelect() {
        return posStorySelect;
    }

    public void setPosStorySelect(int pos) {
        this.posStorySelect = pos;
        if (pos == mListStories.size() - 1) {
            Stories stories = mListStories.get(pos);
            getDataLoadMore(stories.getEndCursor(), stories.getTraySessionId());
        }
    }

    public int getPosItemInList(Stories stories) {
        return mListStories.indexOf(stories);
    }

    public int getSizeListStories() {
        return mListStories.size();
    }

    public String getPreviousStories() {
        if (!isFunLoadRunning) {
            isFunLoadRunning = true;
            if (posStorySelect <= mListStories.size() - 1 && posStorySelect >= 1) {
                posStorySelect--;
                Stories stories = mListStories.get(posStorySelect);
                return String.format(Locale.ENGLISH, Utils.BASE_STORIES_VIEW, stories.getDataBucketId(), stories.getThreadId());
            }
        } else {
            return LOADING;
        }
        isFunLoadRunning = false;
        return null;
    }

    public String getNextStories() {
        if (!isFunLoadRunning) {
            isFunLoadRunning = true;
            if (posStorySelect < mListStories.size() - 1) {
                posStorySelect++;
                Stories stories = mListStories.get(posStorySelect);
                String link = String.format(Locale.ENGLISH, Utils.BASE_STORIES_VIEW, stories.getDataBucketId(), stories.getThreadId());
                if (posStorySelect == mListStories.size() - 1) {
                    getDataLoadMore(mListStories.get(posStorySelect).getEndCursor(), mListStories.get(posStorySelect).getTraySessionId());
                }
                return link;
            }
            if (isLoadMoreRunning) {
                return "";
            }
            isFunLoadRunning = false;
            return null;
        }
        return LOADING;
    }

    public boolean isFunLoadRunning() {
        return isFunLoadRunning;
    }

    public void setFunLoadRunning(boolean funLoadRunning) {
        isFunLoadRunning = funLoadRunning;
    }

    public void updateItemStories(int pos, Stories stories) {
    }

    public void setListener(OnStoriesListener listener) {
        synchronized (mListListener) {
            if (listener != null && !mListListener.contains(listener)) {
                mListListener.add(listener);
            }
        }
    }

    public void removeListener(OnStoriesListener listener) {
        synchronized (mListListener) {
            if (listener != null) {
                mListListener.remove(listener);
            }
        }
    }

    public void updateListCallBack(ArrayList<Stories> stories) {
        synchronized (mListListener) {
            for (OnStoriesListener callBack : mListListener) {
                callBack.onLoadSuccess(stories);
            }
        }
    }

    public void getDataLoadMore(String endCursor, String sessionId) {
        String link = String.format(Locale.ENGLISH, Utils.BASE_LOAD_MORE, endCursor, sessionId);
        loadMoreDataStories(link);
    }

    public Stories getStoriesSelected() {
        return mListStories.get(posStorySelect);
    }

    public ArrayList<Stories> getmListStories() {
        return mListStories;
    }

    public void loadDataFirstTime(String html) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Stories> stories = StoriesFunction.loadData(html);
                if (stories != null && !stories.isEmpty()) {
                    mListStories.addAll(stories);
                }
                if (mListStories != null && !mListStories.isEmpty()) {
                    Stories strories = mListStories.get(mListStories.size() - 1);
                    getDataLoadMore(strories.getEndCursor(), strories.getTraySessionId());
                }
            }
        }).start();
    }

    public void loadDataStoryDetail(String link, String dataBucketId, StoriesFunction.OnLoadDataStoryDetailListener listener) {
        StoriesFunction.loadDataStoryDetail(link, dataBucketId, listener);
    }

    public void loadMoreDataStories(String url) {
        if (!isLoadMoreRunning) {
            isLoadMoreRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document document = Jsoup.connect(url)
                                .userAgent(Utils.getUserAgent())
                                .header(Utils.COOKIE, Utils.getCookie())
                                .timeout(0)
                                .get();
                        String data = document.body().text();
                        ArrayList<Stories> listDataStringObjLoadMore = StoriesFunction.getListDataStringObjLoadMore(data);
                        boolean empty = listDataStringObjLoadMore.isEmpty();
                        if (!empty) {
                            mListStories.addAll(listDataStringObjLoadMore);
                            updateListCallBack(mListStories);
                            isNoDataLoadMore = false;
                            isFunLoadRunning = false;
                        } else {
                            updateListCallBack(null);
                            isNoDataLoadMore = true;
                            isFunLoadRunning = true;
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                    } finally {
                        isLoadMoreRunning = false;
                    }
                }
            }).start();
        }
    }
}
