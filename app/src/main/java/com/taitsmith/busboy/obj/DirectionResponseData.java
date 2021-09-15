package com.taitsmith.busboy.obj;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionResponseData {
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
        List<Double> boundsList;
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
        @SerializedName("distance")
        String distance;
        @SerializedName("start_location")
        double[] coords;
        @SerializedName("summary")
        String summary;
        @SerializedName("steps")
        List<Step> stepList;

        public List<Step> getStepList() {
            return stepList;
        }
    }

    public static class Step {
        @SerializedName("end_location")
        double[] endCoords;
        @SerializedName("start_location")
        double[] startCoords;
    }
}
