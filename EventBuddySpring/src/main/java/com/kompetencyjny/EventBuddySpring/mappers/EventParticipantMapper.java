package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.EventParticipantDto;
import com.kompetencyjny.EventBuddySpring.model.EventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventParticipantMapper {
    @Mapping(target = "eventId", source="id.eventId")
    EventParticipantDto toDto(EventParticipant eventParticipant);
}
