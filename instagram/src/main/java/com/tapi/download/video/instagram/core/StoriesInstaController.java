package com.tapi.download.video.instagram.core;

import android.app.Activity;
import android.util.Log;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.model.StoriesInsta;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class StoriesInstaController {

    private static final String TAG = StoriesInstaController.class.getSimpleName();

    private ArrayList<String> arrID = new ArrayList<>();
    private String base_url = "https://www.instagram.com/";
    private int posStorySelect;
    private final ArrayList<OnStoriesListener> mListListener = new ArrayList<>();
    private ArrayList<StoriesInsta> listStories = new ArrayList<>();

    private static StoriesInstaController instance;

    public static StoriesInstaController getInstance() {
        if (instance == null) {
            instance = new StoriesInstaController();
        }
        return instance;
    }

    public int getPosStorySelect() {
        return posStorySelect;
    }

    public void setPositionStorySelect(int pos) {
        this.posStorySelect = pos;
    }

    public StoriesInsta getStoriesSelected() {
        return listStories.get(posStorySelect);
    }

    public ArrayList<StoriesInsta> getmListStories() {
        return listStories;
    }

    public void setListener(OnStoriesListener listener) {
        synchronized (mListListener) {
            if (listener != null && !mListListener.contains(listener)) {
                Log.e(TAG, "setListener: ");
                mListListener.add(listener);
            }
        }
    }

    public void loadDataFirstTime(final String html) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (html.contains("graphql/query/?query_hash=") && AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "").contains("ds_user_id")) {
                    final ArrayList<StoriesInsta> stories = loadListStory(html);
                    // not clear => duplicate
                    listStories.clear();
                    listStories.addAll(stories);

                    for (OnStoriesListener callBack : mListListener) {
                        callBack.onLoadSuccess(stories);
                        Log.e(TAG, "run: ");
                    }
                }
            }
        }).start();
    }

    private ArrayList<StoriesInsta> loadListStory(String data) {
        ArrayList<StoriesInsta> listStories = new ArrayList<>();
        String userName = "", imageUser = "", thumnailStory = "";
        JSONObject obInto = null;

        arrID.clear();

        getUserIDStory(data);

        String headStatic = "static/bundles/metro/Consumer.js";
        if (!data.contains(headStatic)) {
            headStatic = "static/bundles/es6/Consumer.js";
        }

        String consumer = data.substring(data.indexOf(headStatic));
        consumer = consumer.substring(0, consumer.indexOf("\""));

        if (data.contains(headStatic)) {
            String url = base_url + consumer;
            try {
                Document document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .get();
                String body = document.body().toString();

                String headQuery = "f=50,E=\"";
                if (!body.contains(headQuery)) {
                    headQuery = "L=50,P=\"";
                }

                //get querryHash
                String queryHashFinal = body.substring(body.indexOf(headQuery));
                queryHashFinal = queryHashFinal.substring(queryHashFinal.indexOf("\"") + 1, queryHashFinal.indexOf("\"", queryHashFinal.indexOf("\"") + 1));

                Log.e(TAG + " query_hash ", queryHashFinal);

                if (arrID != null && !arrID.isEmpty()) {
                    //get ID user have story
                    String[] arrStringID = arrID.toArray(new String[arrID.size()]);
                    StringBuffer buffer = new StringBuffer();

                    for (String str : arrStringID) {
                        buffer.append("\"" + str + "\",");
                    }

                    buffer.replace(buffer.lastIndexOf(","), buffer.lastIndexOf(",") + 1, "");

                    //get data story (video, image....)
                    String urlStories = "https://www.instagram.com/graphql/query/?query_hash="
                            + queryHashFinal
                            + "&variables={\"reel_ids\":["
                            + buffer
                            + "],"
                            + "\"tag_names\":[],\"location_ids\":[],\"highlight_reel_ids\":[],"
                            + "\"precomposed_overlay\":false,\"show_story_viewer_list\":true,\"story_viewer_fetch_count\":50,"
                            + "\"story_viewer_cursor\":\"\",\"stories_video_dash_manifest\":false}";

                    Log.e(TAG, "loadListStory: " + urlStories);

                    Document documentDataStories = Jsoup.connect(urlStories)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                            .header("Cookie", AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""))
                            .ignoreContentType(true)
                            .get();

                    String bodyDataStories = documentDataStories.body().toString();

                    // parse JSON
                    if (bodyDataStories.contains("{\"data\"")) {
                        String strObject = bodyDataStories.substring(bodyDataStories.indexOf("{"), bodyDataStories.lastIndexOf("}") + 1);
                        JSONObject object = new JSONObject(strObject);
                        JSONObject obData = object.getJSONObject("data");
                        JSONArray arrReels = obData.getJSONArray("reels_media");

                        for (int i = 0; i < arrReels.length(); i++) {
                            obInto = arrReels.getJSONObject(i);
                            JSONObject obOwner = obInto.getJSONObject("owner");
                            userName = obOwner.getString("username");
                            if (userName.contains("amp;")) {
                                userName = userName.replace("amp;", "");
                            }
                            imageUser = obOwner.getString("profile_pic_url");
                            if (imageUser.contains("amp;")) {
                                imageUser = imageUser.replace("amp;", "");
                            }
                            JSONArray arrItems = obInto.getJSONArray("items");

                            for (int j = 0; j < arrItems.length(); j++) {
                                JSONObject arrInto = arrItems.getJSONObject(j);
                                JSONArray arrDisplay = arrInto.getJSONArray("display_resources");

                                JSONObject obRes = arrDisplay.getJSONObject(0);
                                thumnailStory = obRes.getString("src");
                                if (thumnailStory.contains("amp;")) {
                                    thumnailStory = thumnailStory.replace("amp;", "");
                                }
                            }
                            listStories.add(new StoriesInsta(thumnailStory, userName, imageUser, String.valueOf(obInto), ""));
                        }
                    }
                }

            } catch (IOException e) {
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e(TAG, "loadListStory: " + listStories.size());
        return listStories;
    }

    private void getUserIDStory(String data) {
        if (arrID == null || arrID.isEmpty()) {

            String headGraphql = "graphql/query/?query_hash=";
            String fullGrapql = data.substring(data.indexOf(headGraphql));
            fullGrapql = fullGrapql.substring(0, fullGrapql.indexOf("%7D") + 3);

            if (fullGrapql.contains("amp;")) {
                fullGrapql = fullGrapql.replace("amp;", "");
            }

            String urlJSON = base_url + fullGrapql;

            Log.e(TAG, "getUserIDStory: " + urlJSON);

            try {
                Document document = Jsoup.connect(urlJSON)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .header("Cookie", AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""))
                        .ignoreContentType(true)
                        .get();

                String body = document.body().toString();

                if (body.contains("{\"data\"")) {
                    String strObject = body.substring(body.indexOf("{"), body.lastIndexOf("}") + 1);
                    JSONObject object = new JSONObject(strObject);
                    JSONObject obData = object.getJSONObject("data");
                    JSONObject obUser = obData.getJSONObject("user");
                    JSONObject obFeed = obUser.getJSONObject("feed_reels_tray");
                    JSONObject obEdge = obFeed.getJSONObject("edge_reels_tray_to_reel");
                    JSONArray arrEdges = obEdge.getJSONArray("edges");

                    for (int i = 0; i < arrEdges.length(); i++) {
                        JSONObject obInto = arrEdges.getJSONObject(i);
                        JSONObject obNode = obInto.getJSONObject("node");
                        String obID = obNode.getString("id");

                        arrID.add(obID);

                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

    }

    public void loadDataStoryDetail(String reelsID, LoadDataStoriesDetail.OnLoadDataStoryDetailListener listener) {
        new LoadDataStoriesDetail.loadDataStoryAsync(listener).execute(reelsID);
    }
}
