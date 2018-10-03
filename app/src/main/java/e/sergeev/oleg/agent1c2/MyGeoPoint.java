package e.sergeev.oleg.agent1c2;

import java.util.Date;

//It isn't used in the working version, only for tests of removal of coordinates
class MyGeoPoint {
    private double lat;
    private double longi;
    Date deviceDate, gpsDate;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat ;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public void setDeviceDate(Date deviceDate) {
        this.deviceDate = deviceDate;
    }

    public Date getGpsDate() {
        return gpsDate;
    }

    public void setGpsDate(Date gpsDate) {
        this.gpsDate = gpsDate;
    }
}
