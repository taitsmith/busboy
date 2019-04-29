package utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import java.util.List;

import obj.Bus;

/** A very cool list view adapter to display a list of upcoming buses at the
 * selected stop. Eventually users will be able to select a list item for more
 * info.
 */
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
        ViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_schedule, null);
            holder = new ViewHolder();

            holder.routeName = view.findViewById(R.id.routeNameTextView);
            holder.busPrediction = view.findViewById(R.id.routePredictionTextView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Bus bus = busList.get(position);

        holder.routeName.setText(bus.getRouteName());
        holder.busPrediction.setText(bus.getPredictedDeparture());

        if (bus.getPredictedDelayInSeconds() > 0) {
            holder.busPrediction.setTextColor(Color.RED);
        } else if (bus.getPredictedDelayInSeconds() < 0) {
            holder.busPrediction.setTextColor(Color.parseColor("#2E7D32"));
        }

        return view;
    }

    private class ViewHolder {
        TextView routeName;
        TextView busPrediction;
    }

}
