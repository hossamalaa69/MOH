package com.oleksandr.weshare.Entities;

public class Message {
    String ID;
    String message_text;
    long msg_date;
    String conversation_id;
    String sender_ID;
    String imageUrl;
    String videoUrl;

    public Message(String ID, String message_text, long msg_date, String conversation_id, String sender_ID, String imageUrl, String videoUrl) {
        this.ID = ID;
        this.message_text = message_text;
        this.msg_date = msg_date;
        this.conversation_id = conversation_id;
        this.sender_ID = sender_ID;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    public Message() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMessage_text() {
        return message_text;
    }

    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    public long getMsg_date() {
        return msg_date;
    }

    public void setMsg_date(long msg_date) {
        this.msg_date = msg_date;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getSender_ID() {
        return sender_ID;
    }

    public void setSender_ID(String sender_ID) {
        this.sender_ID = sender_ID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
