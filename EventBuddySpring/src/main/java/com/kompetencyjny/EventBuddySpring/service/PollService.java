package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.model.Poll;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import com.kompetencyjny.EventBuddySpring.repo.PollOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollOptionRepository pollOptionRepository;

    public PollOption getWinner(Poll poll) {
        return pollOptionRepository.findByPoll(poll).stream()
                .max(Comparator.comparingInt(PollOption::getVoteCount))
                .orElse(null);
    }
}
