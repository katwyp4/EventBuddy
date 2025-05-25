package com.kompetencyjny.EventBuddySpring.mappers;

import com.kompetencyjny.EventBuddySpring.dto.ExpenseResponseDto;
import com.kompetencyjny.EventBuddySpring.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(expression = "java(expense.getPayer().getFirstName() + \" \" + expense.getPayer().getLastName())", target = "payerFullName")
    ExpenseResponseDto toDto(Expense expense);
}
