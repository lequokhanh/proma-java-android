package com.nt118.proma.ui.imageselectiton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nt118.proma.R;

import java.util.List;

public class SelectionAdapter extends BaseAdapter {
    private Context context;
    private List<Integer> imageList;
    private int selectedPosition;
    public SelectionAdapter(Context context, List<Integer> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.img_card,null);
        }
        int image = imageList.get(position);
        ImageView imageView = convertView.findViewById(R.id.img_view);
        imageView.setImageResource(image);
        return convertView;
    }
}
