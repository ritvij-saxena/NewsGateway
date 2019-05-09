package com.rj.newsgateway;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    Context context;
    ArrayList<DrawerContent> local_list;
    public MyAdapter(Context context, ArrayList<DrawerContent> list) {
        this.context = context;
        local_list=list;
    }

    @Override
    public int getCount() {
        return local_list.size();
    }

    @Override
    public Object getItem(int position) {
        return local_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view==null){
            view = (LayoutInflater.from(context).inflate(R.layout.my_list_element,parent,false));
        }
        DrawerContent drawerContent = local_list.get(position);
        TextView textView = view.findViewById(R.id.list_element_text);
        textView.setTextColor(drawerContent.getColor());
        textView.setText(drawerContent.getName());
        return  view;
    }
}
