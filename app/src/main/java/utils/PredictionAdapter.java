package utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.taitsmith.busboy.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import obj.Prediction;

/** A very cool list view adapter to display a list of upcoming buses at the
 * selected stop. Eventually users will be able to select a list item for more
 * info.
 */
public class PredictionAdapter extends RealmBaseAdapter<Prediction> implements ListAdapter {
    OrderedRealmCollection<Prediction> predictionList;

    public PredictionAdapter(OrderedRealmCollection<Prediction> realmResults) {
        super(realmResults);
        predictionList = realmResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Prediction prediction;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_schedule, parent, false);

            viewHolder = new ViewHolder();
            prediction = predictionList.get(position);

            viewHolder.busPrediction = convertView.findViewById(R.id.routePredictionTextView);
            viewHolder.routeName = convertView.findViewById(R.id.routeNameTextView);

            String prdtm = prediction.getPrdtm();

            prdtm = prdtm.substring(prdtm.length() - 5);

            String routeName = prediction.getRt().concat(" ").concat(prediction.getRtdir());

            viewHolder.routeName.setText(routeName);
            viewHolder.busPrediction.setText(prdtm);

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
