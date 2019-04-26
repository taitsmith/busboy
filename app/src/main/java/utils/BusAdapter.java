package utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import java.util.List;

import obj.Bus;

public class BusAdapter extends BaseAdapter {
    private List<Bus> busList;
    private LayoutInflater inflater;

    public BusAdapter(Context context, List<Bus> busList) {
        this.busList = busList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return busList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;

        if (view == null) {
            view = inflater.inflate(R.layout.schedule_list_item, null);
            holder = new ViewHolder();

            holder.routeName = view.findViewById(R.id.routeNameTextView);
            holder.busPrediction = view.findViewById(R.id.routePredictionTextView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Bus bus = busList.get(position);

        holder.routeName.setText(bus.getRoute());
        holder.busPrediction.setText(bus.getArrivalTime());

        return view;
    }

    private class ViewHolder {
        TextView routeName;
        TextView busPrediction;
    }

}
