package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.TaskDto;
import com.kompetencyjny.EventBuddySpring.dto.TaskRequest;
import com.kompetencyjny.EventBuddySpring.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    @Mapping(target = "eventId", source = "task.event.id")
    @Mapping(target = "assignedUserId", source = "task.assignedUser.id")
    TaskDto toDto(Task task);

    Task toEntity(TaskRequest taskRequest);
}
