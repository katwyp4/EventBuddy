package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.ExpenseResponseDto;
import com.kompetencyjny.EventBuddySpring.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "event.id", target = "eventId")
    ExpenseResponseDto toDto(Expense expense);
}
