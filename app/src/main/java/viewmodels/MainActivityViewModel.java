package viewmodels;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import obj.Bus;

public class MainActivityViewModel extends ViewModel {

    public static List<Bus> createBusList(JSONArray busArray) throws JSONException {
        List<Bus> busList = new ArrayList<>();

        for (int i = 0; i < busArray.length(); i++) {
            JSONObject bus = busArray.getJSONObject(i);
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

            b.setArrivalTime(eta);
            b.setRoute(route);
            b.setVehicleId(vehicleId);
            b.setDelay(delay);

            busList.add(b);
        }

        return busList;
    }
}
