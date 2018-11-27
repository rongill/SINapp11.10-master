package com.rongill.rsg.sinprojecttest.basic_objects;

import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {

    private String userId, status, userName, userType;
    private ArrayList<User> friends = new ArrayList<>();
    private MyBeacon currentBeacon;

    public User(){}

    public User(String userId, String userName, String status, String userType){
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.userType = userType;

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

        return Objects.hash(getUserId(), getStatus(), userName, getUserType());
    }
}
