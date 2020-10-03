package com.sufnatech.meetingofhearts.Entities;

import java.util.Map;

public class Interest {
    private String ID;
    private String name;
    private Map<String, String> users;

    public Interest(String ID, String name, Map<String, String> users) {
        this.ID = ID;
        this.name = name;
        this.users = users;
    }

    public Interest() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }
}
