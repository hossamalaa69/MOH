package com.sufnatech.meetingofhearts.Entities;

import java.util.Map;

public class Conversation implements Comparable<Conversation> {
    String ID;
    String last_message;
    long last_message_date;
    Map<String, String> users;

    public Conversation(String ID, String last_message, long last_message_date, Map<String, String> users) {
        this.ID = ID;
        this.last_message = last_message;
        this.last_message_date = last_message_date;
        this.users = users;
    }

    public Conversation() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public long getLast_message_date() {
        return last_message_date;
    }

    public void setLast_message_date(long last_message_date) {
        this.last_message_date = last_message_date;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    @Override
    public int compareTo(Conversation conversation) {
        if(this.last_message_date < conversation.getLast_message_date())
            return 1;
        else
            return -1;
    }
}
