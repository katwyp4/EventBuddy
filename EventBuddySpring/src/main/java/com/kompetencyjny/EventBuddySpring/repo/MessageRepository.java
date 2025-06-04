package com.kompetencyjny.EventBuddySpring.repo;

import com.kompetencyjny.EventBuddySpring.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByEventIdOrderBySentAtAsc(Long eventId);
    List<Message> findByEventIdAndSentAtAfterOrderBySentAtAsc(Long eventId, LocalDateTime after);

}
