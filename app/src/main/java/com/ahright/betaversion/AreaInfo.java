package com.ahright.betaversion;

public class AreaInfo {

    double latitude,longitude;
    String address,name,startDate,endDate;
    Integer icon;

    public AreaInfo(double latitude, double longitude, String address, String name, String startDate, String endDate, Integer icon) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.icon = icon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Integer getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "AreaInfo{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", icon=" + icon +
                '}';
    }
}