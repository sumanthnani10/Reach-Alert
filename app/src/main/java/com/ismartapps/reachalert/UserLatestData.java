package com.ismartapps.reachalert;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserLatestData {

    public String targetName,targetAddress,time;
    public LatLng targetlatLng;

    public UserLatestData(){}

    public UserLatestData(String targetName,String targetAddress,LatLng targetlatLng,String time)
    {
        this.targetName=targetName;
        this.targetAddress=targetAddress;
        this.targetlatLng=targetlatLng;
        this.time=time;
    }
}