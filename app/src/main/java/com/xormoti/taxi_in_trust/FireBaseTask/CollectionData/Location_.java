package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

public class Location_ {

    private double latitude; //enlem
    private double longitude; //boylam


    public Location_(){}

    public Location_(double lat, double lng){
        this.latitude=lat;
        this.longitude=lng;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
