package com.nt118.proma.ui.card;

import android.app.Activity;
import android.widget.ArrayAdapter;

import com.nt118.proma.R;
import com.nt118.proma.model.Project;

import java.util.ArrayList;

public class ProjectCardAdapter extends ArrayAdapter<Project> {
    private Activity context;

    public ProjectCardAdapter(Activity context, ArrayList<Project> projects) {
        super(context, R.layout.project_card, projects);
        this.context = context;
    }
}
