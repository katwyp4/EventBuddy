package com.example.myapplication.data;

public class CreateMessageDto {

    private Long   eventId;
    private String content;

    /* ❶  bez-arg constructor – konieczny przy użyciu setEventId()/setContent() */
    public CreateMessageDto() { }


    public Long getEventId()       { return eventId; }
    public void setEventId(Long id){ this.eventId = id; }

    public String getContent()           { return content; }
    public void setContent(String text)  { this.content = text; }
}
