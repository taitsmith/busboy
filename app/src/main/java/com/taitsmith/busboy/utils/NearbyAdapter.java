package com.taitsmith.busboy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import com.taitsmith.busboy.databinding.ListItemNearbyBinding;
import com.taitsmith.busboy.obj.Stop;

import java.util.List;

public class NearbyAdapter extends BaseAdapter {
    List<Stop> stopList;
    ListItemNearbyBinding binding;

    public NearbyAdapter(List<Stop> stopList) {
        this.stopList = stopList;
    }

    @Override
    public int getCount() {
        return stopList.size();
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
        if (stopList.size() == 0) {
            return 1;
        }
        return stopList.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            binding = ListItemNearbyBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false);

            holder = new ViewHolder(binding);
            holder.view = binding.getRoot();
            holder.view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        binding.setStop(stopList.get(position));

        return holder.view;
    }

    private static class ViewHolder {
        private View view;
        ListItemNearbyBinding binding;

        ViewHolder(ListItemNearbyBinding binding) {
            this.view = binding.getRoot();
            this.binding = binding;
        }
    }

}
