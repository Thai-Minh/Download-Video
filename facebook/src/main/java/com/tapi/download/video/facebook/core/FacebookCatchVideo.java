package com.tapi.download.video.facebook.core;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.FacebookLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.ICatch;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.facebook.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacebookCatchVideo implements ICatch {
    private static final String TAG = "FacebookCatchVideo";
    private static FacebookCatchVideo instance;
    private getLinkAnsyn getLinkAnsyn;

    public static FacebookCatchVideo getInstance() {
        if (instance == null)
            instance = new FacebookCatchVideo();
        return instance;
    }

    private static Video getLinkVideo(Document document, String link) {
        String html = document.toString();
        ArrayList<DownloadLink> links = new ArrayList<>();
        Elements elementsByClass = document.getElementsByTag("meta");

        Pattern hdVideo = Pattern.compile("(hd_src):\"(.+?)\"");
        Matcher hdVideoMatcher = hdVideo.matcher(html);

        String linkHd;
        String description = "", type = "", url = "", title = "", image = "", duration;
        String videoId = com.tapi.download.video.core.utils.Utils.getVideoId(link);
        if (hdVideoMatcher.find()) {
            String vUrl = hdVideoMatcher.group();
            linkHd = vUrl.substring(8, vUrl.length() - 1);
            int videoSize = Utils.getVideoSize(linkHd);
            links.add(new FacebookLink(linkHd, videoSize, com.tapi.download.video.core.utils.Utils.VIDEO_HD));

            links.addAll(getListLink(html));
        }
//        duration = getDuration(document);

        for (Element element : elementsByClass) {
            String name = element.attr("name");
            if (name.equalsIgnoreCase("description")) {
                description = element.attr("content");
            }
            String attr = element.attr("property");
            if (attr.equalsIgnoreCase("og:video:type")) {
                type = element.attr("content").replace("video/", ".");
            } else if (attr.equalsIgnoreCase("og:video")) {
                url = element.attr("content").replace("amp;", "");
                int videoSize = Utils.getVideoSize(url);
                links.add(new FacebookLink(url, videoSize, com.tapi.download.video.core.utils.Utils.VIDEO_SD));
            } else if (attr.equalsIgnoreCase("og:title")) {
                title = element.attr("content");
            } else if (attr.equalsIgnoreCase("og:image")) {
                image = element.attr("content").replace("amp;", "");
            }
        }

        if (!title.matches("[a-zA-Z0-9.:?/|]*")) {
            if (title.length() > 220) {
                title = title.substring(0, 220);
            }
            title = title.replaceAll("[:#%?/|.\"]*", "");
            if (title.contains("\n")) {
                title = title.replace("\n", "");
            }
        }

        if (links.isEmpty())
            return null;
        Video video = new Video(title, image, links);
        video.setDuration(StoriesFunction.getDuration(links.get(0).getLink()));
        video.setIdVideo(videoId);
        return video;
    }



    private static String getDuration(Document document) {
        String startIndex = "\"duration\":\"";
        String endIndex = "\",\"uploadDate";

        Element script = document.getElementsByAttribute("lang").first();
        Element head = script.getElementsByTag("head").first();
        String s = head.toString();
        if (s.contains(endIndex))
            return s.substring(s.indexOf(startIndex) + startIndex.length(), s.indexOf(endIndex));
        return "";
    }

    private static ArrayList<DownloadLink> getListLink(String html) {
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Integer> listPos = new ArrayList<>();
        ArrayList<DownloadLink> downloadLinks = new ArrayList<>();
        String word = "FBQualityLabel=\\";
        String end = "VideoPlayerHTML5Shaka";
        for (int i = -1; (i = html.indexOf(word, i + 1)) != -1; i++) {
            listPos.add(i);
        }
        for (int i = 0; i < listPos.size(); i++) {
            if (i != 0) {
                if (i == listPos.size() - 1) {
                    strings.add(html.substring(listPos.get(i - 1), listPos.get(i)));
                    strings.add(html.substring(listPos.get(i), html.lastIndexOf(end)));
                } else {
                    strings.add(html.substring(listPos.get(i - 1), listPos.get(i)));
                }
            }
        }

        String linkAudio = "";
        for (int i = strings.size() - 1; i >= 0; i--) {
            String s = strings.get(i);
            String link = getLink(s, false);
            int videoSize = Utils.getVideoSize(link);
            if (i != strings.size() - 1) {
                FacebookLink facebookLink = new FacebookLink(link, videoSize, getResolution(s));
                facebookLink.setAudioLink(linkAudio);
                downloadLinks.add(facebookLink);
            } else {
                linkAudio = getLink(s, true);
                int videoSize1 = Utils.getVideoSize(linkAudio);
                FacebookLink facebookLink = new FacebookLink(link, videoSize1 + videoSize, getResolution(s));
                facebookLink.setAudioLink(linkAudio);
                downloadLinks.add(facebookLink);
            }
        }
        return downloadLinks;
    }

    private static String getLink(String s, boolean isEnd) {
        String start = "\\\">\\x3CBaseURL>";
        String start1 = "/>\\x3CBaseURL>";
        String end = "\\x3C/BaseURL>";
        return isEnd ? s.substring(s.indexOf(start1) + start.length() - 1, s.lastIndexOf(end)).replaceAll("amp;", "")
                : s.substring(s.indexOf(start) + start.length(), s.indexOf(end)).replaceAll("amp;", "").trim();
    }

    private static int getResolution(String s) {
        String start = "FBQualityLabel=\\\"";
        String end = "\\\">";
        try {
            return Integer.parseInt(s.substring(start.length(), s.indexOf(end)).replace("p", ""));
        } catch (Exception e) {
            return -1;
        }
    }


    @Override
    public void getVideoDownloadLink(String viewLink, OnCatchVideoListener onCatchVideoListener) {
        if (getLinkAnsyn != null) {
            getLinkAnsyn.cancel(true);
            getLinkAnsyn = null;
        }
        getLinkAnsyn = new getLinkAnsyn(onCatchVideoListener);
        getLinkAnsyn.execute(viewLink);
    }

    private static class getLinkAnsyn extends AsyncTask<String, Void, Video> {
        private OnCatchVideoListener listener;
        private String link;

        public getLinkAnsyn(OnCatchVideoListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null)
                listener.onStartCatch();
        }

        @Override
        protected Video doInBackground(String... strings) {
            if (isCancelled()) {
                return null;
            }
            try {
                Random random = new Random();
                link = strings[0];
                int i = random.nextInt(Utils.USER_AGENT.length);
                Document document = Jsoup.connect(link)
                        .userAgent(Utils.USER_AGENT[i]).get();
                return getLinkVideo(document, link);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            if (listener != null) {
                if (video != null)
                    listener.onCatchedLink(video);
                else listener.onPrivateLink(link);
            }
        }
    }
}
