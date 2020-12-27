package com.tapi.download.video.facebook.core;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.utils.Utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StoriesFunction {
    private static final String TAG = "StoriesFunction";

    public static int getDuration(String url) {
        if (url != null && !Utils.checkLinkLive(url)) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(url, new HashMap<String, String>());
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec = Long.parseLong(time);
                retriever.release();
                return (int) timeInMillisec;
            } catch (Exception e) {
                return 0;
            }

        }
        return 0;
    }

    public static ArrayList<Stories> loadData(String data) {
        ArrayList<Stories> strories = new ArrayList<>();
        try {
            Document document = Jsoup.parse(data);
            Element de = document.getElementsByClass("_68de").first();
            Elements elementsByTag = de.getElementsByClass("_46z0 rectangular nineBySixteen");
            for (Element element : elementsByTag) {
                StringBuilder mTitle = new StringBuilder();
                Elements m44 = element.getElementsByClass("_6m44");
                for (Element element1 : m44) {
                    mTitle.append(" ").append(element1.text());
                }
                String mDataBucketId = element.attr("data-bucket-id");
                String href = element.attr("data-href");
                String mEndCursor = getEndCursor(href);
                String mThreadId = getThreadId(href);
                String mImageStory = getImageStories(element);
                String mImageProfile = getImageProfile(element);
                String mTraySessionId = getTraySessionId(href);
                strories.add(new Stories(mImageStory, mTitle.toString().trim(), mImageProfile, mDataBucketId, mThreadId, mEndCursor, mTraySessionId));
            }
        } catch (Exception e) {
            return strories;
        }


        return strories;
    }

    public static String getTraySessionId(String href) {
        int i = href.indexOf("tray_session_id=");
        int i1 = href.indexOf("&source");
        String substring = href.substring(i, i1);
        boolean contains = substring.contains("tray_session_id=");
        if (contains)
            return substring.replace("tray_session_id=", "");
        return null;
    }

    public static String getImageProfile(Element element) {
        Element classImage = element.getElementsByClass("_6mrj nineBySixteen").first();
        Element element1 = classImage.getElementsByClass("_26w4 medium _26w9 _26wu nineBySixteen").first();
        Element first = element1.getElementsByClass("img _1-yc _26w6 profpic").first();
        String style = first.attr("style");
        int i = style.indexOf("h");
        int i2 = style.indexOf("')");
        String substring = style.substring(i, i2);
        String replace = substring.replace("\\3a ", ":");
        String replace1 = replace.replace("\\3d ", "=");
        return replace1.replace("\\26 ", "&");
    }

    public static String getImageStories(Element element) {
        Elements classImage = element.getElementsByClass("_6pvr nineBySixteen");
        Element t2w = classImage.get(0).getElementsByClass("_6t2w").first();
        Element div = t2w.getElementsByTag("img").first();
        return div.attr("src");
    }

    public static String getThreadId(String href) {
        int i = href.indexOf("thread_id=");
        int i1 = href.indexOf("&tray_session_id");
        String substring = href.substring(i, i1);
        int i2 = substring.indexOf("=");
        return substring.substring(i2 + 1);
    }

    public static String getEndCursor(String href) {
        int i = href.indexOf("=");
        int i1 = href.indexOf("&");
        return href.substring(i + 1, i1);
    }

    public static ArrayList<Stories> getListDataStringObjLoadMore(String data) {
        ArrayList<Stories> strories = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Integer> listPos = new ArrayList<>();
        String word = "aria-label";
        String end = ",{\"cmd\":\"script\",\"type\":\"onload\",\"code\":\"";
        for (int i = -1; (i = data.indexOf(word, i + 1)) != -1; i++) {
            listPos.add(i);
        }
        for (int i = 0; i < listPos.size(); i++) {
            if (i % 2 != 0) {
                if (i == listPos.size() - 1) {
                    int i1 = data.indexOf(end);
                    strings.add(data.substring(listPos.get(i - 1), i1));
                } else {
                    strings.add(data.substring(listPos.get(i - 1), listPos.get(i + 1)));
                }
            }
        }
        for (String s : strings) {
            String mTitle = getTitleDataLoadMore(s);
            String mDataBucketId = getBucketIdLoadMore(s);
            String mEndCursor = getEndCursorLoadMore(s);
            String mThreadId = getThreadIdLoadMore(s);
            String mImageStory = getImageStoriesLoadMore(s);
            String mImageProfile = getImageProfileLoadMore(s);
            String mTraySessionId = getTraySessionIdLoadMore(s);
            strories.add(new Stories(mImageStory, mTitle, mImageProfile, mDataBucketId, mThreadId, mEndCursor, mTraySessionId));
        }
        return strories;
    }

    public static String getTraySessionIdLoadMore(String data) {
        String startPos = "tray_session_id=";
        String endPos = "&source";
        return data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos));
    }

    public static String getImageProfileLoadMore(String data) {
        String startPos = "url('";
        String endPos = "') no-repeat center";
        String substring = data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos));
        String replace = substring.replace("\\\\3a ", ":");
        String replace1 = replace.replace("\\\\3d ", "=");
        String replace2 = replace1.replace("\\\\26 ", "&");
        return replace2.replace("\\", "");
    }

    public static String getImageStoriesLoadMore(String data) {
        String startPos = "src=\\\"";
        String endPos = "\\\" class=";
        return data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos)).replace("\\", "");
    }

    public static String getThreadIdLoadMore(String data) {
        String startPos = "thread_id=";
        String endPos = "&tray_session_id";
        return data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos));
    }

    public static String getEndCursorLoadMore(String data) {
        String startPos = "end_cursor=";
        String endPos = "&has_bucket_ids";
        String substring = data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos));
        if (substring.contains("\\u00253D"))
            return substring.replace("\\u00253D", "%3D");
        return substring;
    }

    public static String getBucketIdLoadMore(String data) {
        String startI = "data-bucket-id=\\\"";
        String endI = "\\\" tabindex";
        return data.substring(data.indexOf(startI) + startI.length(), data.indexOf(endI));
    }

    public static String getTitleDataLoadMore(String s) {
        String str = "div class=\\\"_6m44\\\">";
        int i = s.indexOf(str);
        int i1 = s.indexOf("class=\\\"_84p9\\\"");
        String substring = s.substring(i, i1);
        String str1 = "\\u003C";
        String fristName = substring.substring(substring.indexOf(str) + str.length(), substring.indexOf(str1));
        String substring1 = substring.substring(substring.lastIndexOf(str));
        String lastName = substring1.substring(substring1.indexOf(str) + str.length(), substring1.indexOf(str1));
        String s1 = fristName + " " + lastName;
        return StringEscapeUtils.unescapeJava(s1);
    }

    private static String getLastTimeStoriesDetail(Document document) {
        Element viewport = document.body().getElementById("viewport");
        Element first2 = viewport.getElementsByClass("_3-ce").first();
        return first2.select("abbr").text();
    }


    private static String[] getListThreadIDStoriesDetail(String data) {
        String startPos = "threadIDs:[\"";
        String endPos = "\"],startingThreadIndex";
        String substring = data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos)).replaceAll("\"", "");
        return substring.split(",");
    }

    private static String getStringDataStoriesDetail(Document document) {
        try {
            String data = null;
            Elements body = document.body().getElementsByClass("bare touch x2 _fzu _50-3 _67i4 iframe acbk");
            Element element = body.first();
            Elements script = element.getElementsByTag("script");
            for (Element element1 : script) {
                if (element1.toString().contains("threadIDs")) {
                    data = element1.toString();
                    break;
                }
            }
            return data;
        } catch (Exception e) {
            return null;
        }

    }

    private static String getTypeStoryStoriesDetail(String data) {
        String startPos = "mediaType:\"";
        String endPos = "\",ownerID";
        return data.substring(data.indexOf(startPos) + startPos.length(), data.indexOf(endPos));
    }

    private static String getImageHdStoryStoriesDetail(Document document, boolean type) {
        Element body = document.body();
        Element elementsByClass = body.getElementsByClass("_2b-9").first();
        Elements allElements = elementsByClass.getAllElements();
        if (!type)
            return allElements.get(2).attr("src");
        else {
            Element first = elementsByClass.getElementsByClass("videoDiv _6-k2").first();
            return first.getElementsByTag("img").first().attr("src");
        }
    }

    private static String getVideoStoryStoriesDetail(Document document) {
        Element body = document.body();
        Element elementsByClass = body.getElementsByClass("_2b-9").first();
        Element first = elementsByClass.getElementsByClass("videoDiv _6-k2").first();
        Element mw = first.getElementsByClass("_53mw").first();
        String attr = mw.attr("data-store");
        String startPos = "src\":\"";
        String endPos = "\",\"width";
        return attr.substring(attr.indexOf(startPos) + startPos.length(), attr.indexOf(endPos)).replace("\\", "");
    }

    @WorkerThread
    public static void loadDataStoryDetail(String link, String dataBucketId, OnLoadDataStoryDetailListener listener) {

        Stories stories = new Stories();
        try {
            Document document = Jsoup.connect(link)
                    .userAgent(Utils.getUserAgent())
                    .header(Utils.COOKIE, Utils.getCookie())
                    .get();
            String data = getStringDataStoriesDetail(document);
            if (data == null) {
                if (listener != null) {
                    listener.onLoadSuccess(null, null);
                    StoriesFaceBookController.getInstance().setFunLoadRunning(false);
                }
            } else {
                String videoStoryStoriesDetail = null;
                String[] listThreadID = getListThreadIDStoriesDetail(data);
                stories.setListThreadId(listThreadID);
                stories.setSize(listThreadID.length);
                String typeStory = getTypeStoryStoriesDetail(data);
                boolean video = typeStory.equalsIgnoreCase("video");
                if (video)
                    videoStoryStoriesDetail = getVideoStoryStoriesDetail(document);
                stories.setType(typeStory);
                stories.setLastTime(getLastTimeStoriesDetail(document));
                stories.setImageStoryHd(getImageHdStoryStoriesDetail(document, video));
                stories.setVideoStory(video ? videoStoryStoriesDetail : "");
                stories.setmVideoSize(Utils.getVideoSize(videoStoryStoriesDetail));
                stories.setDuration(getDuration(videoStoryStoriesDetail));
                if (listener != null) {
                    listener.onLoadSuccess(stories, dataBucketId);
                    StoriesFaceBookController.getInstance().setFunLoadRunning(false);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: " + e.getMessage());
        }


    }

    public interface OnLoadDataStoryDetailListener {
        void onLoadSuccess(Stories stories, String dataBucketId);
    }
}
