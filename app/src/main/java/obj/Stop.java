package obj;

import io.realm.RealmObject;

public class Stop extends RealmObject {
    private String StopId, Name, Latitude, Longitue;

    public String getStopId() {
        return StopId;
    }

    public void setStopId(String stopId) {
        StopId = stopId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitue() {
        return Longitue;
    }

    public void setLongitue(String longitue) {
        Longitue = longitue;
    }
}
