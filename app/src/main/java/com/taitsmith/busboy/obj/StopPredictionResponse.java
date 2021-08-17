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
    private String error;

    public BustimeResponse getBustimeResponse() {
        return bustimeResponse;
    }

    public String getError() {
        return error;
    }

    public static class BustimeResponse {

        @SerializedName("prd")
        @Expose
        private List<Prediction> prd = null;

        public List<Prediction> getPrd() {
            return prd;
        }

        public static class Prediction {
            @SerializedName("tmstmp")
            @Expose
            private String tmstmp;
            @SerializedName("typ")
            @Expose
            private String typ;
            @SerializedName("stpid")
            @Expose
            private String stpid;
            @SerializedName("stpnm")
            @Expose
            private String stpnm;
            @SerializedName("vid")
            @Expose
            private String vid;
            @SerializedName("dstp")
            @Expose
            private Integer dstp;
            @SerializedName("rt")
            @Expose
            private String rt;
            @SerializedName("rtdd")
            @Expose
            private String rtdd;
            @SerializedName("rtdir")
            @Expose
            private String rtdir;
            @SerializedName("des")
            @Expose
            private String des;
            @SerializedName("prdtm")
            @Expose
            private String prdtm;
            @SerializedName("dly")
            @Expose
            private Boolean dly;
            @SerializedName("dyn")
            @Expose
            private Integer dyn;
            @SerializedName("tablockid")
            @Expose
            private String tablockid;
            @SerializedName("tatripid")
            @Expose
            private String tatripid;
            @SerializedName("prdctdn")
            @Expose
            private String prdctdn;
            @SerializedName("zone")
            @Expose
            private String zone;
            @SerializedName("nbus")
            @Expose
            private String nbus;

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
        }
    }
}

