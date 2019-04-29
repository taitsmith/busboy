package utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import obj.Bus;

/** A very cool list view adapter to display a list of upcoming buses at the
 * selected stop. Eventually users will be able to select a list item for more
 * info.
 */
public class BusAdapter extends RealmBaseAdapter<Bus> implements ListAdapter {
    OrderedRealmCollection<Bus> busList;

    public BusAdapter(OrderedRealmCollection<Bus> realmResults) {
        super(realmResults);
        busList = realmResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_schedule, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.busPrediction = convertView.findViewById(R.id.routePredictionTextView);
            viewHolder.routeName = convertView.findViewById(R.id.routeNameTextView);

            String s = busList.get(position).getPredictedDeparture();

            s = s.substring(s.length() - 8, s.length() - 3);

            viewHolder.routeName.setText(busList.get(position).getRouteName());
            viewHolder.busPrediction.setText(s);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView routeName;
        TextView busPrediction;
    }

}
