package com.taitsmith.busboy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.taitsmith.busboy.databinding.ListItemScheduleBinding;
import com.taitsmith.busboy.api.StopPredictionResponse.BustimeResponse.Prediction;

import java.util.List;

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
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (predictionList.size() == 0) {
            return 1;
        }
        return predictionList.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        Prediction p = predictionList.get(position);

        if (view == null) {
            binding = ListItemScheduleBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false);

            holder = new ViewHolder(binding);
            holder.view = binding.getRoot();
            holder.view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        binding.setPrediction(p);

        return holder.view;
    }

    private static class ViewHolder {
        private View view;
        ListItemScheduleBinding binding;

        ViewHolder(ListItemScheduleBinding binding) {
            this.view = binding.getRoot();
            this.binding = binding;
        }
    }
}
