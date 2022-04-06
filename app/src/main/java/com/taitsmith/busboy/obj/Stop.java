package com.taitsmith.busboy.obj;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

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

    String linesServed;

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

    public String getLinesServed() { return linesServed; }

    public void setLinesServed(String linesServed) { this.linesServed = linesServed; }
}
