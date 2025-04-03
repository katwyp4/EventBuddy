package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.model.Poll;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import com.kompetencyjny.EventBuddySpring.repo.PollOptionRepository;
import com.kompetencyjny.EventBuddySpring.repo.PollRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;

    public PollController(PollRepository pollRepository,
                          PollOptionRepository pollOptionRepository) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
    }

    // [POST] /api/polls
    @PostMapping
    public Poll createPoll(@RequestBody Poll poll) {
        return pollRepository.save(poll);
    }

    // Dodanie opcji do ankiety
    // [POST] /api/polls/{pollId}/options
    @PostMapping("/{pollId}/options")
    public ResponseEntity<PollOption> addOption(@PathVariable Long pollId, @RequestBody PollOption option) {
        Optional<Poll> pollOpt = pollRepository.findById(pollId);
        if (pollOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Poll poll = pollOpt.get();
        poll.addOption(option);
        pollRepository.save(poll);
        return ResponseEntity.ok(option);
    }

    // Głosowanie na opcję
    // [POST] /api/polls/{pollId}/options/{optionId}/vote
    @PostMapping("/{pollId}/options/{optionId}/vote")
    public ResponseEntity<PollOption> vote(@PathVariable Long pollId,
                                           @PathVariable Long optionId) {
        Optional<PollOption> optionOpt = pollOptionRepository.findById(optionId);
        if (optionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PollOption option = optionOpt.get();
        if (!option.getPoll().getId().equals(pollId)) {
            // bezpieczeństwo: ta opcja nie należy do podanego poll
            return ResponseEntity.badRequest().build();
        }

        // Zwiększamy licznik głosów
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);

        return ResponseEntity.ok(option);
    }
}
