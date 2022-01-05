package com.taitsmith.busboy.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
When we call the AC Transit API to get nearby stops, we also want to figure out which way those
 stops go (NB/SB/EB/WB). The call to find nearby stops doesn't include this, so we need to make
 a separate call for each stop returned to find which direction it goes.

 **/
public class StopDestinationResponse {
    @SerializedName("StopId")
    @Expose
    public Integer stopId;
    @SerializedName("Status")
    @Expose
    public String status;
    @SerializedName("RouteDestinations")
    @Expose
    public List<RouteDestination> routeDestinations = null;

    public static class RouteDestination {

        @SerializedName("RouteId")
        @Expose
        public String routeId;
        @SerializedName("DirectionId")
        @Expose
        public Integer directionId;
        @SerializedName("Direction")
        @Expose
        public String direction;
        @SerializedName("Destination")
        @Expose
        public String destination;
        @SerializedName("FinalPassingTime")
        @Expose
        public String finalPassingTime;
        @SerializedName("Status")
        @Expose
        public String status;

    }
}
