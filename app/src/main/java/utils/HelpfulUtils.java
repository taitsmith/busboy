package utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import obj.Bus;

/**
 * Hello friend, and welcome to the {@link HelpfulUtils} class! Here we will store all
 * of our helpful utils to keep them out of the way.
 */

public class HelpfulUtils {

    //takes a json array from AC Transit's API and turns it into a list of buses
    //arriving at the selected stop and their arrival times.
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

            b.setArrivalTime(eta);
            b.setRoute(route);
            b.setVehicleId(vehicleId);
            b.setDelay(delay);

            busList.add(b);

            Log.d("BUS: ", route.concat(" at ").concat(eta));
        }

        return busList;
    }
}
