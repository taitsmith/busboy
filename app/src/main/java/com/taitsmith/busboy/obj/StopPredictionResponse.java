package com.taitsmith.busboy.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StopPredictionResponse {

    @SerializedName("bustime-response")
    @Expose
    private BustimeResponse bustimeResponse;
    @SerializedName("error")
    @Expose
    private BusError error;

    public BustimeResponse getBustimeResponse() {
        return bustimeResponse;
    }

    public BusError getErrors() {
        return error;
    }

    public static class BusError {
        @SerializedName("msg")
        String message;

        public String getMessage() {
            return message;
        }
    }

    public static class BustimeResponse {

        @SerializedName("prd")
        @Expose
        private final List<Prediction> prd = null;

        public List<Prediction> getPrd() {
            return prd;
        }

        public static class Prediction {
            @SerializedName("stpnm")
            @Expose
            private String stpnm;
            @SerializedName("vid")
            @Expose
            private String vid;
            @SerializedName("rt")
            @Expose
            private String rt;
            @SerializedName("rtdir")
            @Expose
            private String rtdir;
            @SerializedName("des")
            @Expose
            private String des;
            @SerializedName("prdtm")
            @Expose
            private String prdtm;
            @SerializedName("dyn")
            @Expose
            private Integer dyn;
            @SerializedName("prdctdn")
            @Expose
            private String prdctdn;

            public String getRt() {
                return rt;
            }

            public String getRtdir() {
                return rtdir;
            }

            public String getDes() {
                return des;
            }

            public String getPrdtm() {
                return prdtm;
            }

            public String getStpnm() { return stpnm; }

            public String getVid() {return vid;}

            public Integer getDyn() { return dyn; }

            public String getPrdctdn() { return prdctdn; }

            public void setPrdctdn(String prdctdn) { this.prdctdn = prdctdn;}
        }
    }
}

