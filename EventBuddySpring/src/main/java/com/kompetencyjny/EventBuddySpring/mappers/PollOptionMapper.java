package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.PollOptionDto;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PollOptionMapper {

    @Mapping(source = "poll.id", target = "pollId")
    PollOptionDto toDto(PollOption option);

    @Mapping(target ="poll", ignore = true)
    PollOption toEntity(PollOptionDto dto);
}
