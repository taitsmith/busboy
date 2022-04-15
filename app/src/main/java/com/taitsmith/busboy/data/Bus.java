package com.taitsmith.busboy.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bus  {
    @SerializedName("VehicleId")
    @Expose
    public Integer vehicleId;
    @SerializedName("CurrentTripId")
    @Expose
    public Integer currentTripId;
    @SerializedName("Latitude")
    @Expose
    public Double latitude;
    @SerializedName("Longitude")
    @Expose
    public Double longitude;
    @SerializedName("Heading")
    @Expose
    public Integer heading;
    @SerializedName("TimeLastReported")
    @Expose
    public String timeLastReported;

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
