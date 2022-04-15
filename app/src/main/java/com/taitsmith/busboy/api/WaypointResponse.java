package com.taitsmith.busboy.api;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WaypointResponse {
    @SerializedName("Patterns")
    @Expose
    private final List<Pattern> patterns = null;

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public static class Pattern {

        @SerializedName("Waypoints")
        @Expose
        private final List<Waypoint> waypoints = null;

        public List<Waypoint> getWaypoints() {
            return waypoints;
        }

        public static class Waypoint {
            @SerializedName("Latitude")
            @Expose
            private Double latitude;
            @SerializedName("Longitude")
            @Expose
            private Double longitude;

            public LatLng getLatLng() {
                return new LatLng(latitude,longitude);
            }
        }
    }
}
