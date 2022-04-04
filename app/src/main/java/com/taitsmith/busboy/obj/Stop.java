package com.taitsmith.busboy.obj;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Stop  {
    @SerializedName("StopId")
    String stopId;
    @SerializedName("Name")
    String name;
    @SerializedName("Latitude")
    Double latitude;
    @SerializedName("Longitude")
    Double longitude;
    @SerializedName("ScheduledTime")
    Date scheduledTime;

    public String getStopId() {
        return stopId;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }
}
