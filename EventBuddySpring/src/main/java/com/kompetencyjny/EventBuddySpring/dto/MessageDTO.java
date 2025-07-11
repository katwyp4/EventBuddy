package com.kompetencyjny.EventBuddySpring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {

    private Long id;
    private String content;
    private LocalDateTime sentAt;


    private Long senderId;

    @JsonProperty("senderFullName")
    private String senderFullName;

    private Long eventId;
}
