package utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import obj.Stop;

/** A very cool list view adapter to display a list of nearby stops.
 * TODO includes adding lines served at each stop (one thing at a time).
 */
public class NearbyAdapter extends RealmBaseAdapter<Stop> implements ListAdapter {
    OrderedRealmCollection<Stop> stopList;

    public NearbyAdapter(OrderedRealmCollection<Stop> realmResults) {
        super(realmResults);
        stopList = realmResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Stop stop;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_nearby, parent, false);

            viewHolder = new ViewHolder();
            stop = stopList.get(position);

            viewHolder.stopNameTV = convertView.findViewById(R.id.stopNameTextView);

            String stopName = stop.getName();

            viewHolder.stopNameTV.setText(stopName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView stopNameTV; //form of street + cross street
    }

}
