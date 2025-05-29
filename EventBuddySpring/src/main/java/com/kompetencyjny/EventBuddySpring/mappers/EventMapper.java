package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.EventDto;
import com.kompetencyjny.EventBuddySpring.dto.EventRequest;
import com.kompetencyjny.EventBuddySpring.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { PollMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    @Mapping(target = "datePoll", source = "datePoll")
    @Mapping(target = "locationPoll", source = "locationPoll")
    @Mapping(target = "budgetDeadline", source = "budgetDeadline")
    EventDto toDto(Event event);

    @Mapping(target = "datePoll", source = "datePoll")
    @Mapping(target = "locationPoll", source = "locationPoll")
    @Mapping(target = "budgetDeadline", source = "budgetDeadline")
    Event toEntity(EventRequest eventRequest);
}
