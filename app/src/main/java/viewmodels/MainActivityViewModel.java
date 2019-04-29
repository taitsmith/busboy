package viewmodels;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import obj.Bus;

public class MainActivityViewModel extends ViewModel {


    public static List<Bus> createBusList(JSONArray busArray) throws JSONException {
        Realm realm = Realm.getDefaultInstance();

        List<Bus> busList = new ArrayList<>();

        for (int i = 0; i < busArray.length(); i++) {
            JSONObject bus = busArray.getJSONObject(i);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObjectFromJson(Bus.class, bus);
                }
            });

            Bus b = new Bus();

            String route = bus.getString("RouteName");
            String eta = bus.getString("PredictedDeparture");
            String vehicleId = bus.getString("VehicleId");
            int delay = bus.getInt("PredictedDelayInSeconds");

            eta = eta.substring(eta.length() - 8);
            eta = LocalTime.parse(eta).toString();

            if (delay > 0) {
                b.setStatus("Late");
            } else if (delay < 0) {
                b.setStatus("Early");
            } else {
                b.setStatus("On Time");
            }

            b.setPredictedDeparture(eta);
            b.setRouteName(route);
            b.setVehicleId(vehicleId);
            b.setPredictedDelayInSeconds(delay);

            busList.add(b);
        }

        return busList;
    }
}
