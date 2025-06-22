package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.PollOptionDto;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PollOptionMapper {

    @Mapping(source = "poll.id", target = "pollId")
    @Mapping(source = "option.value_", target = "value")
    PollOptionDto toDto(PollOption option);

    @Mapping(target ="poll", ignore = true)
    @Mapping(target ="value_", source = "dto.value")
    PollOption toEntity(PollOptionDto dto);
}
