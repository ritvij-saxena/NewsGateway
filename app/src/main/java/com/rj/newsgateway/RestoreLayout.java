package com.rj.newsgateway;

import java.io.Serializable;
import java.util.ArrayList;


public class RestoreLayout implements Serializable {
    ArrayList<Source> sourceList = new ArrayList<Source>();
    ArrayList<Article> articleList = new ArrayList<Article>();
    ArrayList<String> categories = new ArrayList<String>();
    int currentSource;
    int currentArticle;

    public ArrayList<Source> getSourceList() {
        return sourceList;
    }

    public void setSourceList(ArrayList<Source> sourceList) {
        this.sourceList = sourceList;
    }

    public ArrayList<Article> getArticleList() {
        return articleList;
    }

    public void setArticleList(ArrayList<Article> articleList) {
        this.articleList = articleList;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public int getCurrentSource() {
        return currentSource;
    }

    public void setCurrentSource(int currentSource) {
        this.currentSource = currentSource;
    }

    public int getCurrentArticle() {
        return currentArticle;
    }

    public void setCurrentArticle(int currentArticle) {
        this.currentArticle = currentArticle;
    }
}
