package com.taitsmith.busboy.utils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.databinding.ListItemScheduleBinding;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import java.util.List;

/** A very cool list view adapter to display a list of upcoming buses at the
 * selected stop. Eventually users will be able to select a list item for more
 * info.
 */
public class PredictionAdapter extends BaseAdapter {
    List<Prediction> predictionList;
    ListItemScheduleBinding binding;

    public PredictionAdapter(List<Prediction> predictionList) {
       this.predictionList = predictionList;
    }

    @Override
    public int getCount() {
        return predictionList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        binding = ListItemScheduleBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                viewGroup, false);

        binding.setPrediction(predictionList.get(i));

        return binding.getRoot();
    }
}
