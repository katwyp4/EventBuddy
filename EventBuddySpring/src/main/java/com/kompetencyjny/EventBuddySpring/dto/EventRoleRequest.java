package com.kompetencyjny.EventBuddySpring.dto;

import com.kompetencyjny.EventBuddySpring.model.EventRole;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRoleRequest {
    @Pattern(regexp = "^(PASSIVE|ACTIVE|ADMIN)$", message = "Role has to be PASSIVE ACTIVE or ADMIN")
    private String role;

    public EventRole toEventRoleEnum(){
        return EventRole.valueOf(role);
    }
}
