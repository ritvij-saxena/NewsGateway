package com.rj.newsgateway;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class SourceDownloaderAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "SourceDownloaderAsyncTask";
    StringBuilder stringBuilder;
    boolean noDataFound = false;
    boolean isNoDataFound = true;
    MainActivity mainActivity;
    String category;
    Uri.Builder buildURL = null;
    ArrayList<Source> sourceList = new ArrayList<Source>();
    ArrayList<String> categoryList = new ArrayList<String>();
    String API_KEY = "48e5f56e5b8048659006bdc2d4680cbd";
    String NewsAPI;

    public SourceDownloaderAsyncTask(MainActivity ma, String category) {
        mainActivity = ma;
        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("")) {
            this.category = "";
            NewsAPI = "https://newsapi.org/v2/sources?language=en&country=us&apiKey=" + API_KEY;
        } else {
            String api1 = "https://newsapi.org/v2/sources?language=en&country=us&category=";
            String api2 = "&apiKey=" + API_KEY;
            NewsAPI = api1 + category + api2;
            this.category = category;
        }

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        for (int j = 0; j < sourceList.size(); j++) {
            String temp = sourceList.get(j).getSourceCategory();
            if (!categoryList.contains(temp))
                categoryList.add(temp);
        }
        mainActivity.setSources(sourceList, categoryList);
    }

    @Override
    protected String doInBackground(String... strings) {

        buildURL = Uri.parse(NewsAPI).buildUpon();
        connectToAPI();
        if (!isNoDataFound) {
            parseJSON1(stringBuilder.toString());
        }
        return null;
    }


    public void connectToAPI() {

        String urlToUse = buildURL.build().toString();
        stringBuilder = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                noDataFound = true;
            } else {
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                isNoDataFound = false;

            }
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
//            Log.d(TAG, "FileNotFoundException ");
        } catch (Exception e) {
            e.printStackTrace();
//            Log.d(TAG, "Exception doInBackground: " + e.getMessage());
        }
    }


    private void parseJSON1(String s) {
        try {
            if (!noDataFound) {
                JSONObject jObjMain = new JSONObject(s);
                JSONArray sources = jObjMain.getJSONArray("sources");
                for (int i = 0; i < sources.length(); i++) {
                    JSONObject src = (JSONObject) sources.get(i);
                    Source srcObj = new Source();
                    srcObj.setSourceId(src.getString("id"));
                    srcObj.setSourceCategory(src.getString("category"));
                    srcObj.setSourceName(src.getString("name"));
                    srcObj.setSourceUrl(src.getString("url"));
                    sourceList.add(srcObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
