package com.rongill.rsg.sinprojecttest.basic_objects;

import java.io.Serializable;

public class ManagementUser extends User implements Serializable {

    private String structure;

    public ManagementUser(){}

    public ManagementUser(String userId, String userName, String status, String userType, String structure){
        super(userId, userName, status, userType);
        this.structure = structure;
    }

    public String getStructure() {
        return structure;
    }
    public void setStructure(String structure) {
        this.structure = structure;
    }
}
