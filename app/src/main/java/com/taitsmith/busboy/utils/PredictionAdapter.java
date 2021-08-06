package com.taitsmith.busboy.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import java.util.List;

/** A very cool list view adapter to display a list of upcoming buses at the
 * selected stop. Eventually users will be able to select a list item for more
 * info.
 */
public class PredictionAdapter extends BaseAdapter {
    List<Prediction> predictionList;

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
        return null;
    }

    private static class ViewHolder {
        TextView routeName;
        TextView busPrediction;
    }

}
