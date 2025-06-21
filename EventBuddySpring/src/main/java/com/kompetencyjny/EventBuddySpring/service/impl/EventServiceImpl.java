package com.kompetencyjny.EventBuddySpring.service.impl;

import com.kompetencyjny.EventBuddySpring.exception.NotFoundException;
import com.kompetencyjny.EventBuddySpring.exception.ForbiddenException;
import com.kompetencyjny.EventBuddySpring.model.*;
import com.kompetencyjny.EventBuddySpring.repo.EventParticipantRepository;
import com.kompetencyjny.EventBuddySpring.repo.EventRepository;
import com.kompetencyjny.EventBuddySpring.repo.ExpenseRepository;
import com.kompetencyjny.EventBuddySpring.service.EventService;
import com.kompetencyjny.EventBuddySpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventParticipantRepository eventParticipantRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    @Override
    public Event create(Event event, String loggedUserName) {
        event.setId(null);

        Optional<User> loggedUserOpt = userService.findByEmail(loggedUserName);
        if (loggedUserOpt.isEmpty()) {
            throw new NotFoundException("!!! YOU SHOULD NOT SEE THIS !!! Cannot find logged in user! email: \"" + loggedUserName + "\".\nThis method expects to get a email of logged in user.");
        }

        if (event.getEnableDateVoting() && event.getDatePoll() != null) {
            Poll datePoll = event.getDatePoll();
            datePoll.setQuestion("Wybierz date wydarzenia");
            datePoll.setEvent(event);
            if (datePoll.getOptions() != null) {
                datePoll.getOptions().forEach(opt -> opt.setPoll(datePoll));
            }
            else{
                datePoll.setQuestion("zle");
            }
        }

        if (event.getEnableLocationVoting() && event.getLocationPoll() != null) {
            Poll locationPoll = event.getLocationPoll();
            locationPoll.setQuestion("Wybierz lokalizacje wydarzenia");
            locationPoll.setEvent(event);
            if (locationPoll.getOptions() != null) {
                locationPoll.getOptions().forEach(opt -> opt.setPoll(locationPoll));
            }
        }

        event.addParticipant(loggedUserOpt.get(), EventRole.ADMIN);

        return this.eventRepository.save(event);
    }

    @Override
    public Map<String, BigDecimal> calculateBalances(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Set<EventParticipant> participants = event.getParticipants();
        if (participants.isEmpty()) {
            return Map.of();
        }

        List<Expense> expenses = expenseRepository.findByEventId(eventId);

        // Oblicz ile wydał każdy użytkownik
        Map<User, BigDecimal> totalPaidByUser = new HashMap<>();
        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            totalPaidByUser.put(payer,
                    totalPaidByUser.getOrDefault(payer, BigDecimal.ZERO).add(expense.getAmount()));
        }

        // Oblicz całkowity koszt i średni koszt na osobę
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int numParticipants = participants.size();
        BigDecimal sharePerUser = numParticipants > 0
                ? totalAmount.divide(BigDecimal.valueOf(numParticipants), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Oblicz saldo każdego uczestnika
        Map<String, BigDecimal> balances = new HashMap<>();
        for (EventParticipant ep : participants) {
            User user = ep.getUser();
            BigDecimal paid = totalPaidByUser.getOrDefault(user, BigDecimal.ZERO);
            BigDecimal balance = paid.subtract(sharePerUser);
            balances.put(user.getEmail(), balance);
        }

        return balances;
    }


    public boolean hasDateVotingEnded(Event event) {
        return event.getDatePollDeadline() != null && LocalDate.now().isAfter(event.getDatePollDeadline());
    }
    public boolean hasLocationVotingEnded(Event event) {
        return event.getLocationPollDeadline() != null && LocalDate.now().isAfter(event.getLocationPollDeadline());
    }

    @Override
    public Page<Event> findAllVisible(Pageable pageable, String loggedUserName) {
        Optional<User> userOpt = userService.findByEmail(loggedUserName);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User whith loggedUserName: "+loggedUserName+" does not exist");
        }

        Long userId = userOpt.get().getId();
        return eventRepository.findEventsVisibleToUser(userId, pageable);
    }

    @Override
    public Page<Event> findAllPublic(Pageable pageable) {

        return eventRepository.findPublicEvents(pageable);
    }

    @Override
    public Page<Event> findAllInternal(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }


    @Override
    public Optional<Event> findVisibleById(Long id, String loggedUserName) {
            Optional<Event> eventOpt = eventRepository.findById(id);
            if (eventOpt.isEmpty()) return eventOpt;
            Boolean isActive = eventOpt.get().getActive();
            if (isActive==null || !isActive) return Optional.empty();
            if (!isEventVisibleToUser(eventOpt.get(), loggedUserName)) return Optional.empty();
            return eventOpt;
    }

    @Override
    public Optional<Event> findPublicById(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) return eventOpt;
        Event event = eventOpt.get();
        Boolean isActive = event.getActive();
        if (isActive==null || !isActive) return Optional.empty();
        if (event.getEventPrivacy().equals(EventPrivacy.PRIVATE)) return Optional.empty();
        return eventOpt;
    }

    @Override
    public Optional<Event> findByIdInternal(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) return eventOpt;
        Boolean isActive = eventOpt.get().getActive();
        if (isActive==null || !isActive) return Optional.empty();
        return eventOpt;
    }

    private boolean isEventVisibleToUser(Event event, String userName){
        if (event.getEventPrivacy().equals(EventPrivacy.PUBLIC_CLOSED)) return true;
        if (event.getEventPrivacy().equals(EventPrivacy.PUBLIC_OPEN)) return true;
        Optional<User> user = userService.findByEmail(userName);
        if (user.isEmpty()) return false;
        if (user.get().getRole()==Role.ADMIN) return true;
        return isUserAParticipantOf(event.getId(), user.get().getId());
    }

    private boolean isEventVisibleToUser(Long eventId, String userName){
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) return false;
        return isEventVisibleToUser(eventOpt.get(), userName);
    }

    @Override
    public boolean existsById(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) return false;
        Boolean isActive =  eventOpt.get().getActive();
        return (isActive != null) && isActive;

    }

    @Override
    public Event fullUpdate(Long id, Event event, String loggedUserName) {
        if (!this.existsById(id)) throw new RuntimeException("Trying to update non existing event!");
        if (!isUserPermitted(id, loggedUserName, EventRole.ADMIN))
            throw new ForbiddenException("User username:"+loggedUserName+" not allowed to update event "+id);

        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public void deleteById(Long id, String loggedUserName) {
        Optional<Event> existingEventOpt = eventRepository.findById(id);
        if (existingEventOpt.isEmpty()) throw new RuntimeException("Trying to delete non existing event!");

        Event existingEvent = existingEventOpt.get();
        if (!isUserPermitted(id, loggedUserName, EventRole.ADMIN))
            throw new ForbiddenException("User username:"+loggedUserName+" not allowed to delete event "+id);

        if (!existingEvent.getActive()) return;
        existingEvent.setActive(false);
        existingEvent.setDeactivationDate(LocalDate.now());
        eventRepository.save(existingEvent);
    }

    @Override
    public boolean isUserPermitted(Long eventId, String email, EventRole minRole) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) throw new RuntimeException("No user found of email "+email);

        User loggedInUser = userOpt.get();
        if (loggedInUser.getRole() == Role.ADMIN) return true;

        Optional<EventParticipant> eventParticipantOpt = eventParticipantRepository.findById_EventIdAndId_UserId(eventId, loggedInUser.getId());
        if (eventParticipantOpt.isEmpty()) return false;

        return eventParticipantOpt.get().getEventRole().compareTo(minRole)>=0;
    }

    @Override
    public boolean isUserAParticipantOf(Long eventId, Long userId) {
        Optional<Event> eventOpt = this.findByIdInternal(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new NotFoundException("User or Event does not exists!");
        return eventParticipantRepository.existsById(new UserEventId(userId, eventId));
    }

    @Transactional
    @Override
    public EventParticipant addEventParticipant(Long eventId, Long userId, EventRole role, String loggedUserName){
        Optional<Event> eventOpt = this.findByIdInternal(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new NotFoundException("User or Event does not exists!");
        Event event = eventOpt.get();
        User user = userOpt.get();
        if (!isEventVisibleToUser(event, loggedUserName)) throw new NotFoundException("User or Event does not exists!");

        if (!isUserPermitted(eventId, loggedUserName, EventRole.ADMIN))
            throw new ForbiddenException("User username:"+loggedUserName+" not allowed to add a participant to event: "+eventId);

        EventParticipant eventParticipant = event.addParticipant(user, role);
        eventRepository.save(event);
        return eventParticipant;
    }

    @Override
    public void removeEventParticipant(Long eventId, Long userId, String loggedUserName){
        Optional<Event> eventOpt = this.findByIdInternal(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new RuntimeException("User or Event does not exists!");
        Event event = eventOpt.get();
        User user = userOpt.get();
        if (!isEventVisibleToUser(event, loggedUserName)) throw new NotFoundException("User or Event does not exists!");

        Optional<User> loggedUserOpt = userService.findByEmail(loggedUserName);
        if (loggedUserOpt.isEmpty()) throw  new NotFoundException("Logged user does not exist");
        User loggedUser = loggedUserOpt.get();

        if (!isUserPermitted(eventId, loggedUserName, EventRole.ADMIN) && !loggedUser.equals(user))
            throw new ForbiddenException("User username:"+loggedUserName+" not allowed to remove participants from event: "+eventId);

        event.removeParticipant(user);
        eventRepository.save(event);
    }

    @Override
    public Optional<EventParticipant> getEventParticipantInternal(Long eventId, Long userId) {
        return eventParticipantRepository.findById_EventIdAndId_UserId(eventId, userId);
    }

    @Override
    public Optional<EventParticipant> getEventParticipant(Long eventId, Long userId, String loggedUserName) {
        if (!isEventVisibleToUser(eventId, loggedUserName)) return Optional.empty();
        return eventParticipantRepository.findById_EventIdAndId_UserId(eventId, userId);
    }

    @Override
    public Page<EventParticipant> findAllEventParticipants(Pageable pageable, Long eventId, String loggedUserName) {
        if (!isEventVisibleToUser(eventId, loggedUserName)) throw new NotFoundException("Event does not exists!");
        return eventParticipantRepository.findAllById_EventId(eventId, pageable);
    }

    public EventParticipant joinEvent(Long eventId, String loggedEmail) {
        User user = userService.findByEmail(loggedEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = findByIdInternal(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Optional<EventParticipant> existing = eventParticipantRepository.findById(new UserEventId(user.getId(), eventId));
        if (existing.isPresent()) return existing.get();

        if (event.getEventPrivacy() == EventPrivacy.PRIVATE) {
            throw new ForbiddenException("Nie można dołączyć do prywatnego wydarzenia.");
        }

        EventParticipant participant = event.addParticipant(user, EventRole.PASSIVE);
        eventRepository.save(event);
        return participant;
    }

    @Override
    public EventParticipant updateEventParticipantRole(Long eventId, Long userId, EventRole eventRole, String loggedEmail){
        Optional<Event> eventOpt = this.findByIdInternal(eventId);
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty() || eventOpt.isEmpty()) throw new NotFoundException("Event or user does not exist! eventId: "+eventId+" userId "+ userId);

        Event event = eventOpt.get();
        User user = userOpt.get();

        Optional<User> loggedUserOpt = userService.findByEmail(loggedEmail);
        if (loggedUserOpt.isEmpty()) throw new NotFoundException("Logged user not found!");

        User loggedUser = loggedUserOpt.get();

        if (!isEventVisibleToUser(event, loggedEmail)) throw new NotFoundException("User or Event does not exists! eventId: "+eventId+" userId "+userId);

        if (!isUserPermitted(eventId, loggedEmail, EventRole.ADMIN) && user != loggedUser)
            throw new ForbiddenException("User username:"+loggedEmail+" not allowed to change user's role for event "+eventId);

        if (user == loggedUser && loggedUser.getRole()!=Role.ADMIN) {
            Optional<EventParticipant> eventParticipantOpt = eventParticipantRepository.findById(new UserEventId(userId, eventId));
            if (eventParticipantOpt.isEmpty() && eventRole.compareTo(EventRole.PASSIVE)>0)
                throw new ForbiddenException("User username:" + loggedEmail + " not allowed to change user's role for event " + eventId);
            if (eventParticipantOpt.isEmpty() && !event.getEventPrivacy().equals(EventPrivacy.PUBLIC_OPEN))
                throw new ForbiddenException("User username:" + loggedEmail + " not allowed to change user's role for event " + eventId);
            if (eventParticipantOpt.isPresent() && eventParticipantOpt.get().getEventRole().compareTo(eventRole)<0)
                throw new ForbiddenException("User username:" + loggedEmail + " not allowed to change user's role for event " + eventId);
        }

        Optional<EventParticipant> eventParticipantOpt = eventParticipantRepository.findById_EventIdAndId_UserId(eventId, userId);
        if (eventParticipantOpt.isEmpty()){
            EventParticipant eventParticipant = event.addParticipant(user, eventRole);
            eventRepository.save(event);
            return eventParticipant;
        }

        EventParticipant eventParticipant = eventParticipantOpt.get();
        eventParticipant.setEventRole(eventRole);
        return eventParticipantRepository.save(eventParticipant);
    }

    public Page<Event> findAllEventsOfUser(Pageable pageable, Long userId, String loggedUserName){
        Optional<User> userOpt = this.userService.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("User does not exist! UserId: "+ userId);

        Optional<User> loggedInOpt = userService.findByEmail(loggedUserName);
        if (loggedInOpt.isEmpty()) throw new NotFoundException("Logged in user does not exists!");

        return eventRepository.findAllEventsOfUser(pageable, userId, loggedInOpt.get().getId());
    }
}
