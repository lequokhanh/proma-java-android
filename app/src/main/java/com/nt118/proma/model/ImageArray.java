package com.nt118.proma.model;

import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageArray {
    public ArrayList<Integer> getAvatarImage() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.avatar1);
        images.add(R.drawable.avatar2);
        images.add(R.drawable.avatar3);
        images.add(R.drawable.avatar4);
        images.add(R.drawable.avatar5);
        images.add(R.drawable.avatar6);
        images.add(R.drawable.avatar7);
        images.add(R.drawable.avatar8);
        images.add(R.drawable.avatar9);
        images.add(R.drawable.avatar10);
        images.add(R.drawable.avatar11);
        images.add(R.drawable.avatar12);
        images.add(R.drawable.avatar13);
        images.add(R.drawable.avatar14);
        images.add(R.drawable.avatar15);
        images.add(R.drawable.avatar16);
        images.add(R.drawable.avatar17);
        images.add(R.drawable.avatar18);
        images.add(R.drawable.avatar19);
        images.add(R.drawable.avatar20);
        images.add(R.drawable.avatar21);
        images.add(R.drawable.avatar22);
        images.add(R.drawable.avatar23);
        return (ArrayList<Integer>) images;
    }

    public static ArrayList<Integer> getCoverProjectImage() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.cover1);
        images.add(R.drawable.cover2);
        images.add(R.drawable.cover3);
        images.add(R.drawable.cover4);
        images.add(R.drawable.cover5);
        images.add(R.drawable.cover6);
        images.add(R.drawable.cover7);
        images.add(R.drawable.cover8);
        images.add(R.drawable.cover9);
        images.add(R.drawable.cover10);
        images.add(R.drawable.cover11);
        images.add(R.drawable.cover12);
        images.add(R.drawable.cover13);
        images.add(R.drawable.cover14);
        images.add(R.drawable.cover15);
        images.add(R.drawable.cover16);
        images.add(R.drawable.cover17);
        images.add(R.drawable.cover18);
        return (ArrayList<Integer>) images;
    }

    public static Map<String, Integer> getIconTaskCard() {
        Map<String, Integer> icons = new HashMap<>();
        icons.put("Meeting", R.drawable.ic_meeting);
        icons.put("Task", R.drawable.ic_task_square);
        return icons;
    }
}
