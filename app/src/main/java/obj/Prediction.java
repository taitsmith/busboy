package obj;

import io.realm.RealmObject;

/**
 * Class for holding predictions to be parsed from AC Transit's API response and sent to
 * list view etc.
 * Strings as follows:
 * stpnm = stop name (ie Shattuck Av + Allston Way)
 * stpid = stop id (ie 55555)
 * rt = route
 * rtdir = route direction (ie To Lake Merrit BART)
 * des = destination (ie Lake Merritt BART via MLK Jr Way)
 * prdtm = predicted time (returns in form YYYYMMDD HH:MM)
 * dly = delay (boolean)
 */

public class Prediction extends RealmObject {
    private String stpnm, stpid, rt, rtdir, des, prdtm;
    private boolean dly;

    public String getStpnm() {
        return stpnm;
    }

    public void setStpnm(String stpnm) {
        this.stpnm = stpnm;
    }

    public String getStpid() {
        return stpid;
    }

    public void setStpid(String stpid) {
        this.stpid = stpid;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getRtdir() {
        return rtdir;
    }

    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getPrdtm() {
        return prdtm;
    }

    public void setPrdtm(String prdtm) {
        this.prdtm = prdtm;
    }

    public boolean isDly() {
        return dly;
    }

    public void setDly(boolean dly) {
        this.dly = dly;
    }
}
