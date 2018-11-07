package com.rongill.rsg.sinprojecttest;

import java.util.ArrayList;

public class User {

    private String userId, status, userName;


    public User(){}

    public User(String userId, String userName, String status){
        this.userId = userId;
        this.userName = userName;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }






}
