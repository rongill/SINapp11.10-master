package com.rongill.rsg.sinprojecttest.basic_objects;

import java.io.Serializable;


public class MaintenanceUser extends User implements Serializable {

    private String structure;

    public MaintenanceUser(String userId, String userName, String status, String userType, String structure) {
        super(userId, userName, status, userType);
        this.structure = structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getStructure() {
        return structure;
    }
}
