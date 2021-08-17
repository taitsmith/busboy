package com.taitsmith.busboy.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BusRoute {
        @SerializedName("RouteId")
        @Expose
        private String routeId;
        @SerializedName("Name")
        @Expose
        private String name;
        @SerializedName("Description")
        @Expose
        private String description;

        public String getRouteId() {
            return routeId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
}
