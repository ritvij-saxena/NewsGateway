package com.rj.newsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    boolean serviceRunning = false;
    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    static final String ARTICLE_LIST = "ARTICLE_LIST";
    static final String SOURCE_ID = "SOURCE_ID";
    ArrayList<String> source_list = new ArrayList<String>();
    ArrayList<String> category_list = new ArrayList<String>();
    ArrayList<Source> sourceArrayList = new ArrayList<Source>();
    ArrayList<Article> articleArrayList = new ArrayList<Article>();
    HashMap<String, Source> sourceDataMap = new HashMap<>();
    Menu options_menu;
    NewsReceiver newsReceiver;
    String currentNewsSource;
    MyAdapter adapter;
    MyPageAdapter pageAdapter;
    List<Fragment> fragments;
    ViewPager pager;
    boolean boolean_flag;
    int currentSourcePointer;
    ArrayList<DrawerContent> drawerContents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!serviceRunning && savedInstanceState == null) {
            Intent intent = new Intent(MainActivity.this, NewsService.class);
            startService(intent);
            serviceRunning = true;
        }
        newsReceiver = new NewsReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(newsReceiver, filter);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);


        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                                               @Override
                                               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                   pager.setBackgroundResource(0);
                                                   currentSourcePointer = position;
                                                   selectItem(position);
                                               }
                                           }
        );

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        adapter = new MyAdapter(this, drawerContents);
        mDrawerList.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewPager);
        pager.setAdapter(pageAdapter);

        if (sourceDataMap.isEmpty() && savedInstanceState == null)
            new SourceDownloaderAsyncTask(this, "").execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        new SourceDownloaderAsyncTask(this, item.getTitle().toString()).execute();
        colorMenuOptions(item);
        mDrawerLayout.openDrawer(mDrawerList);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void selectItem(int position) {
        currentNewsSource = source_list.get(position);
        Intent intent = new Intent(MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_ID, currentNewsSource);
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: start");
        getMenuInflater().inflate(R.menu.action_menu, menu);
        options_menu = menu;
        if (boolean_flag) {
            options_menu.add("All");
            for (String s : category_list) {
                options_menu.add(s);
            }
        }
        Log.d(TAG, "onCreateOptionsMenu: end");
        return true;
    }
    private void colorMenuOptions(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "business":
                setColor(item, Color.BLUE);
                break;
            case "entertainment":
                setColor(item, Color.GREEN);
                break;
            case "sports":
                setColor(item, Color.YELLOW);
                break;
            case "science":
                setColor(item, Color.MAGENTA);
                break;
            case "technology":
                setColor(item, Color.RED);
                break;
            case "general":
                setColor(item, Color.CYAN);
                break;
            case "health":
                setColor(item, Color.LTGRAY);

        }
    }
    private void setColor(MenuItem item, int color) {
        SpannableString spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
        item.setTitle(spannableString);
    }

    public void setSources(ArrayList<Source> sourceList, ArrayList<String> categoryList) {
        sourceDataMap.clear();
        source_list.clear();
        sourceArrayList.clear();
        drawerContents.clear();
        sourceArrayList.addAll(sourceList);

        for (int i = 0; i < sourceList.size(); i++) {
            source_list.add(sourceList.get(i).getSourceName());
            sourceDataMap.put(sourceList.get(i).getSourceName(), sourceList.get(i));
        }

        if (!options_menu.hasVisibleItems()) {
            category_list.clear();
            category_list = categoryList;
            options_menu.add("All");
            Collections.sort(categoryList);
            for (String s : categoryList)
                options_menu.add(s);
        }

        for (Source s : sourceList) {
            DrawerContent drawerContent = new DrawerContent();
            switch (s.getSourceCategory()) {
                case "business":
                    drawerContent.setColor(Color.BLUE);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "entertainment":
                    drawerContent.setColor(Color.GREEN);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "sports":
                    drawerContent.setColor(Color.YELLOW);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "science":
                    drawerContent.setColor(Color.MAGENTA);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "technology":
                    drawerContent.setColor(Color.RED);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "general":
                    drawerContent.setColor(Color.CYAN);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
                    break;
                case "health":
                    drawerContent.setColor(Color.LTGRAY);
                    drawerContent.setName(s.getSourceName());
                    drawerContents.add(drawerContent);
            }
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "setSources: ");
    }


    private void setFragments(ArrayList<Article> articles) {
        setTitle(currentNewsSource);
        for (int i = 0; i < pageAdapter.getCount(); i++) {
            pageAdapter.notifyChangeInPosition(i);
        }
        fragments.clear();
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            fragments.add(ArticleFragment.newInstance(a, i, articles.size()));
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
        articleArrayList = articles;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        Intent intent = new Intent(MainActivity.this, NewsReceiver.class);
        stopService(intent);
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        RestoreLayout restoreLayout = new RestoreLayout();
        restoreLayout.setCategories(category_list);
        restoreLayout.setSourceList(sourceArrayList);
        restoreLayout.setCurrentArticle(pager.getCurrentItem());
        restoreLayout.setCurrentSource(currentSourcePointer);
        restoreLayout.setArticleList(articleArrayList);
        outState.putSerializable("state", restoreLayout);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        RestoreLayout restoreLayout1 = (RestoreLayout) savedInstanceState.getSerializable("state");
        boolean_flag = true;
        articleArrayList = restoreLayout1 != null ? restoreLayout1.getArticleList() : null;
        category_list = restoreLayout1 != null ? restoreLayout1.getCategories() : null;
        sourceArrayList = restoreLayout1 != null ? restoreLayout1.getSourceList() : null;
        for (int i = 0; i < (sourceArrayList != null ? sourceArrayList.size() : 0); i++) {
            source_list.add(sourceArrayList.get(i).getSourceName());
            sourceDataMap.put(sourceArrayList.get(i).getSourceName(), sourceArrayList.get(i));
        }
        mDrawerList.clearChoices();
        adapter.notifyDataSetChanged();
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                                               @Override
                                               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                   pager.setBackgroundResource(0);
                                                   currentSourcePointer = position;
                                                   selectItem(position);

                                               }
                                           }
        );
        setTitle("News Gateway");
    }

    class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_NEWS_STORY:
                    ArrayList<Article> artList;
                    if (intent.hasExtra(ARTICLE_LIST)) {
                        artList = (ArrayList<Article>) intent.getSerializableExtra(ARTICLE_LIST);
                        setFragments(artList);
                    }
                    break;
            }
        }
    }


    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return baseId + position;
        }

        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }


    }
}
