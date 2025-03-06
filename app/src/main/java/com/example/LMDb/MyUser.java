package com.example.LMDb;

public class MyUser {
   private String userName;
    private int yob;
    private String profileImageUrl;


    public MyUser(){

    }
    public MyUser(String userName, int yob){
        this.userName = userName;
        this.yob = yob;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getYob() {
        return yob;
    }

    public void setYob(int yob) {
        this.yob = yob;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public MyUser(int yob) {
        this.yob = yob;
    }

    public MyUser(String userName) {
        this.userName = userName;
    }

    public String getUserName(){
            return userName;
    }
}
