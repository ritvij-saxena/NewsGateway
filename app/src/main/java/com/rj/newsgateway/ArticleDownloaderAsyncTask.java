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


public class ArticleDownloaderAsyncTask extends AsyncTask<String, Integer, String> {
    String sourceId;
    NewsService service;
    String API_KEY = "48e5f56e5b8048659006bdc2d4680cbd";
    String ARTICLE_QUERY_1 = "https://newsapi.org/v2/everything?sources=";
    String ARTICLE_QUERY_2 = "&apiKey=" + API_KEY;
    Uri.Builder buildURL = null;
    StringBuilder stringBuilder;
    boolean noDataFound = false;
    boolean isNoDataFound = true;
    ArrayList<Article> articleArrayList = new ArrayList<Article>();

    public ArticleDownloaderAsyncTask(NewsService service, String sourceId) {
        this.sourceId = sourceId;
        this.service = service;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        service.setArticles(articleArrayList);
    }

    @Override
    protected String doInBackground(String... strings) {
        String query = "";

        query = ARTICLE_QUERY_1 + sourceId + ARTICLE_QUERY_2;
//        buildURL.appendQueryParameter("language","en");
//        buildURL.appendQueryParameter("pageSize","100");
        buildURL = Uri.parse(query).buildUpon();
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
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void parseJSON1(String s) {
        try {
            if (!noDataFound) {
                JSONObject jObjMain = new JSONObject(s);
                JSONArray articles = jObjMain.getJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject art = (JSONObject) articles.get(i);
                    Article artObj = new Article();
                    artObj.setAuthor(art.getString("author"));
                    artObj.setDescription(art.getString("description"));
                    artObj.setPublishingDate(art.getString("publishedAt"));
                    artObj.setTitle(art.getString("title"));
                    artObj.setUrlToImage(art.getString("urlToImage"));
                    artObj.setArticleUrl(art.getString("url"));
                    articleArrayList.add(artObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
