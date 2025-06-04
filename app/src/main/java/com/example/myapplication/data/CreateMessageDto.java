package com.example.myapplication.data;

public class CreateMessageDto {
    private Long senderId;
    private Long eventId;
    private String content;

    public CreateMessageDto() {}  // potrzebne dla Gson

    public CreateMessageDto(Long senderId, Long eventId, String content) {
        this.senderId = senderId;
        this.eventId  = eventId;
        this.content  = content;
    }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
