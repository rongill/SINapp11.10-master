package com.rongill.rsg.sinprojecttest.basic_objects;

import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {

    private String userId, status, username, userType;
    private ArrayList<User> friends = new ArrayList<>();
    private MyBeacon currentBeacon;

    public User(){}

    public User(String userId, String userName, String status, String userType){
        this.userId = userId;
        this.username = userName;
        this.status = status;
        this.userType = userType;
        currentBeacon = new MyBeacon();

    }

    public MyBeacon getCurrentBeacon() {
        return currentBeacon;
    }

    public void setCurrentBeacon(MyBeacon currentBeacon) {
        this.currentBeacon = currentBeacon;
        //TODO get beacon (X,Y) from DB.
    }

    public void setFriends(ArrayList<User> friendList){
        this.friends = friendList;
    }

    public ArrayList<User> getFriends(){
        return this.friends;
    }

    public void addFriend(User friend){
        this.friends.add(friend);
    }

    public void setFriendStatusByUid(String friendUid, String newStatus){
        for(int i=0; i<friends.size();i++){
            if(friends.get(i).getUserId().equals(friendUid))
                this.friends.get(i).setStatus(newStatus);

        }

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserType(String userType){
        this.userType = userType;
    }

    public String getUserType(){
        return this.userType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return (Objects.equals(getUserId(), user.getUserId()) &&
                Objects.equals(getStatus(), user.getStatus()) &&
                Objects.equals(getUsername(), user.getUsername()) &&
                Objects.equals(getUserType(), user.getUserType()));
    }

    @Override
    public int hashCode() {

        return Objects.hash(getUserId(), getStatus(), username, getUserType());
    }
}
