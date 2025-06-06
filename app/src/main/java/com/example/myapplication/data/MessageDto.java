package com.example.myapplication.data;
import com.google.gson.annotations.SerializedName;

public class MessageDto {
    private Long id;
    private String content;
    private String sentAt;          // "2025-05-17T13:55:00"

    @SerializedName("senderFullName")
    private String senderFullName;
    private Long eventId;

    public MessageDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public String getSenderFullName() { return senderFullName; }
    public void setSenderFullName(String senderFullName) { this.senderFullName = senderFullName; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}
