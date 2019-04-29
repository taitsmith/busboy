package obj;

import io.realm.RealmObject;

public class Bus extends RealmObject {

    private String RouteName, PredictedDeparture, VehicleId, status;
    private int PredictedDelayInSeconds;

    public String getRouteName() {
        return RouteName;
    }

    public void setRouteName(String routeName) {
        this.RouteName = routeName;
    }

    public String getPredictedDeparture() {
        return PredictedDeparture;
    }

    public void setPredictedDeparture(String predictedDeparture) {
        this.PredictedDeparture = predictedDeparture;
    }

    public int getPredictedDelayInSeconds() {
        return PredictedDelayInSeconds;
    }

    public void setPredictedDelayInSeconds(int predictedDelayInSeconds) {
        this.PredictedDelayInSeconds = predictedDelayInSeconds;
    }

    public String getVehicleId() {
        return VehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.VehicleId = vehicleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
