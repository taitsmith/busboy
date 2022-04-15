package com.taitsmith.busboy.api;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionResponse {
    @SerializedName("geocoded_waypoints")
    List<Object> geoWaypoints;
    @SerializedName("routes")
    List<MapRoute> routeList;
    @SerializedName("status")
    String status;

    public List<MapRoute> getRouteList() {
        return routeList;
    }

    public static class MapRoute {
        @SerializedName("bounds")
        Object boundsList;
        @SerializedName("legs")
        List<Leg> tripList;
        @SerializedName("warnings")
        String[] warnings;

        public List<Leg> getTripList() {
            return tripList;
        }

        public String[] getWarnings() {
            return warnings;
        }
    }

    public static class Leg {
        @SerializedName("steps")
        List<Step> stepList;

        public List<Step> getStepList() {
            return stepList;
        }
    }

    public static class Step {
        @SerializedName("end_location")
        EndCoords endCoords;

        public EndCoords getEndCoords() {
            return endCoords;
        }
    }

    public static class EndCoords {
        @SerializedName("lat")
        Double lat;
        @SerializedName("lng")
        Double lon;

        public LatLng returnCoords() {
            return new LatLng(this.lat, this.lon);
        }
    }
}
