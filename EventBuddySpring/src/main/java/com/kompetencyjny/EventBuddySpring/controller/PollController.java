package com.kompetencyjny.EventBuddySpring.controller;

import com.kompetencyjny.EventBuddySpring.dto.*;
import com.kompetencyjny.EventBuddySpring.exception.NotFoundException;
import com.kompetencyjny.EventBuddySpring.model.Poll;
import com.kompetencyjny.EventBuddySpring.model.PollOption;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.model.Vote;
import com.kompetencyjny.EventBuddySpring.repo.PollOptionRepository;
import com.kompetencyjny.EventBuddySpring.repo.PollRepository;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import com.kompetencyjny.EventBuddySpring.repo.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    public PollController(PollRepository pollRepository,
                          PollOptionRepository pollOptionRepository,
                          UserRepository userRepository,
                          VoteRepository voteRepository) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    // [POST] /api/polls – tworzenie ankiety
    @PostMapping
    public ResponseEntity<PollDto> createPoll(@RequestBody CreatePollDto createPollDto) {
        Poll poll = new Poll();
        poll.setQuestion(createPollDto.getQuestion());
        // poll.setType(createPollDto.getType()); // jeśli masz
        pollRepository.save(poll);

        PollDto response = new PollDto(poll.getId(), poll.getQuestion(), List.of());
        return ResponseEntity.ok(response);
    }

    // [POST] /api/polls/{pollId}/options – dodawanie opcji
    @PostMapping("/{pollId}/options")
    public ResponseEntity<PollOptionDto> addOption(@PathVariable Long pollId, @RequestBody CreatePollOptionDto dto) {
        Optional<Poll> pollOpt = pollRepository.findById(pollId);
        if (pollOpt.isEmpty()) return ResponseEntity.notFound().build();

        Poll poll = pollOpt.get();
        PollOption option = new PollOption();
        option.setValue(dto.getValue());
        option.setVoteCount(0);
        option.setPoll(poll);
        pollOptionRepository.save(option);

        PollOptionDto response = new PollOptionDto(option.getId(), option.getValue(), option.getVoteCount(), poll.getId());
        return ResponseEntity.ok(response);
    }

    // [POST] /api/polls/{pollId}/options/{optionId}/vote – głosowanie
    @PostMapping("/{pollId}/options/{optionId}/vote")
    public ResponseEntity<?> vote(
            @PathVariable Long pollId,
            @PathVariable Long optionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<PollOption> optionOpt = pollOptionRepository.findById(optionId);
        if (optionOpt.isEmpty()) return ResponseEntity.notFound().build();

        PollOption option = optionOpt.get();
        Poll poll = option.getPoll();

        if (!poll.getId().equals(pollId)) return ResponseEntity.badRequest().build();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Użytkownik nie znaleziony"));

        // Sprawdź, czy użytkownik już głosował
        Optional<Vote> existingVote = voteRepository.findByUserAndPoll(user, poll);
        if (existingVote.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Użytkownik już oddał głos w tej ankiecie.");
        }

        // Dodaj głos
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPoll(poll);
        vote.setOption(option);
        voteRepository.save(vote);

        PollOptionDto response = new PollOptionDto(option.getId(), option.getValue(), option.getVoteCount(), pollId);
        return ResponseEntity.ok(response);
    }


    // [GET] /api/polls/{pollId}/results – wyniki głosowania
    @GetMapping("/{pollId}/results")
    public ResponseEntity<?> getPollResults(@PathVariable Long pollId) {
        Optional<Poll> pollOpt = pollRepository.findById(pollId);
        if (pollOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<PollOptionDto> results = pollOpt.get().getOptions().stream()
                .map(opt -> new PollOptionDto(opt.getId(), opt.getValue(), opt.getVoteCount(), opt.getPoll().getId()))
                .toList();

        return ResponseEntity.ok(results);
    }

    // [GET] /api/polls/{pollId} – pobranie ankiety z opcjami
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDto> getPoll(@PathVariable Long pollId) {
        Optional<Poll> pollOpt = pollRepository.findById(pollId);
        if (pollOpt.isEmpty()) return ResponseEntity.notFound().build();

        Poll poll = pollOpt.get();
        List<PollOptionDto> options = poll.getOptions().stream()
                .map(opt -> new PollOptionDto(opt.getId(), opt.getValue(), opt.getVoteCount(), opt.getPoll().getId()))
                .toList();

        PollDto dto = new PollDto(poll.getId(), poll.getQuestion(), options);
        return ResponseEntity.ok(dto);
    }
}

