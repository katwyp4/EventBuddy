package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.PollDto;
import com.kompetencyjny.EventBuddySpring.model.Poll;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PollOptionMapper.class})
public interface PollMapper {
    PollDto toDto(Poll poll);
    Poll toEntity(PollDto dto);

    @AfterMapping
    default void linkPollOptions(@MappingTarget Poll poll) {
        if (poll.getOptions() != null) {
            for (PollOption option : poll.getOptions()) {
                option.setPoll(poll);
            }
        }
    }

}

