package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.dto.PollOptionDto;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    @Query("SELECT o FROM PollOption o WHERE o.poll.id = (SELECT e.datePoll.id FROM Event e WHERE e.id = :eventId)")
    List<PollOption> findDatePollOptionsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT o FROM PollOption o WHERE o.poll.id = (SELECT e.locationPoll.id FROM Event e WHERE e.id = :eventId)")
    List<PollOption> findLocationPollOptionsByEventId(@Param("eventId") Long eventId);

}
