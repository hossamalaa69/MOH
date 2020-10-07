package com.oleksandr.weshare.Entities;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String ID;
    private String Uid;
    private String user_name;
    private String email;
    private String phone;
    private String imageUrl;
    private String gender;
    private int age;
    private String nationality;
    private String status;
    private Map<String, String> interests;
    private Map<String, String> messaging;

    @Exclude
    private boolean isExpanded;

    public User() {
        this.ID = "";
        this.Uid = "";
        this.user_name = "";
        this.email = "";
        this.phone = "";
        this.imageUrl = "";
        this.gender = "";
        age = 0;
        this.nationality = "";
        this.interests = new HashMap<>();
        this.messaging = new HashMap<>();
        status = "";
        isExpanded = false;
    }

    public User(String ID, String uid, String user_name, String email, String phone, String imageUrl
            , String gender, int age, String nationality, Map<String,String> interests, Map<String, String> messaging, String status) {
        this.ID = ID;
        Uid = uid;
        this.user_name = user_name;
        this.email = email;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.gender = gender;
        this.age = age;
        this.nationality = nationality;
        this.interests = interests;
        this.messaging = messaging;
        this.status = status;
        isExpanded = false;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Map<String, String> getInterests() {
        return interests;
    }

    public void setInterests(Map<String, String> interests) {
        this.interests = interests;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public Map<String, String> getMessaging() {
        return messaging;
    }

    public void setMessaging(Map<String, String> messaging) {
        this.messaging = messaging;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public boolean isExpanded() {
        return isExpanded;
    }

    @Exclude
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
