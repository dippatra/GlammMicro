package com.myglammmicro.models;

public class Restaurants {
    private String resID;
    private String resName;
    private String resCoverUrl;
    private String resRating;
    private String locality;
    private String address;
    private String phoneNO;
    private String ratingColor;
    private String cuisines;

    public Restaurants(String resID,String resName,String resCoverUrl,String resRating,String locality,String address,String phoneNO,String cuisines,String ratingColor){
        this.resID=resID;
        this.resName=resName;
        this.resCoverUrl=resCoverUrl;
        this.resRating=resRating;
        this.locality=locality;
        this.address=address;
        this.phoneNO=phoneNO;
        this.cuisines=cuisines;
        this.ratingColor=ratingColor;

    }

    public String getResID() {
        return resID;
    }

    public String getResName() {
        return resName;
    }

    public String getResCoverUrl() {
        return resCoverUrl;
    }

    public String getResRating() {
        return resRating;
    }

    public String getLocality() {
        return locality;
    }

    public String getAddress() {
        return address;
    }



    public String getPhoneNO() {
        return phoneNO;
    }

    public String getRatingColor() {
        return ratingColor;
    }

    public String getCuisines() {
        return cuisines;
    }
}
