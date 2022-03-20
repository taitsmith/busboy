package com.taitsmith.busboy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.taitsmith.busboy.databinding.ListItemNearbyBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination;

import java.util.HashMap;
import java.util.List;

public class NearbyAdapter extends BaseAdapter {
    HashMap<String, List<RouteDestination>> destinationList;
    List<Stop> stopList;
    ListItemNearbyBinding binding;

    public NearbyAdapter(HashMap<String, List<RouteDestination>> destinationList,
                         List<Stop> stopList) {
        this.destinationList = destinationList;
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
        StringBuilder builder = new StringBuilder();
        Stop stop = stopList.get(position);
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

        binding.setStop(stop);
        try {
            for (RouteDestination destination : destinationList.get(stop.getStopId())) {
                builder.append(destination.routeId)
                        .append(" ")
                        .append(destination.destination)
                        .append("\n");
            }

            binding.listItemLinesServed.setText(builder.toString());
        } catch (NullPointerException e) {
            binding.listItemLinesServed.setText("No data on lines served");
        }

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
