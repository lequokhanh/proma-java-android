package com.nt118.taskmanager.ui.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.nt118.taskmanager.databinding.FragmentProjectBinding;

import java.util.ArrayList;
import java.util.List;

public class ProjectFragment extends Fragment {

    private FragmentProjectBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProjectViewModel projectViewModel =
                new ViewModelProvider(this).get(ProjectViewModel.class);

        binding = FragmentProjectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AnyChartView anyChartView = binding.anychartview;
        Pie pie = AnyChart.pie();

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("John", 10000));
        data.add(new ValueDataEntry("Jake", 12000));
        data.add(new ValueDataEntry("Peter", 18000));
        pie.data(data);
        anyChartView.setChart(pie);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}