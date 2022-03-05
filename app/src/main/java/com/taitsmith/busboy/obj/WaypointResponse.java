package com.taitsmith.busboy.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WaypointResponse {
    @SerializedName("Booking")
    @Expose
    private String booking;
    @SerializedName("RouteAlpha")
    @Expose
    private String routeAlpha;
    @SerializedName("Patterns")
    @Expose
    private List<Pattern> patterns = null;

    public String getBooking() {
        return booking;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public static class Pattern {
        @SerializedName("DirectionId")
        @Expose
        private Integer directionId;
        @SerializedName("Direction")
        @Expose
        private String direction;
        @SerializedName("Destination")
        @Expose
        private String destination;
        @SerializedName("FirstPlaceId")
        @Expose
        private String firstPlaceId;
        @SerializedName("LastPlaceId")
        @Expose
        private String lastPlaceId;
        @SerializedName("IsDefault")
        @Expose
        private Boolean isDefault;
        @SerializedName("TotalDistance")
        @Expose
        private Integer totalDistance;
        @SerializedName("Waypoints")
        @Expose
        private List<Waypoint> waypoints = null;

        public List<Waypoint> getWaypoints() {
            return waypoints;
        }

        public static class Waypoint {
            @SerializedName("OrderID")
            @Expose
            private Integer orderID;
            @SerializedName("Latitude")
            @Expose
            private Double latitude;
            @SerializedName("Longitude")
            @Expose
            private Double longitude;
            @SerializedName("Heading")
            @Expose
            private Double heading;
            @SerializedName("DistanceToNextStop")
            @Expose
            private Integer distanceToNextStop;
            @SerializedName("DistanceFromStart")
            @Expose
            private Integer distanceFromStart;
            @SerializedName("StopSequence")
            @Expose
            private Integer stopSequence;

            public Double getLatitude() {
                return latitude;
            }

            public Double getLongitude() {
                return longitude;
            }
        }
    }
}
