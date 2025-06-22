package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.exception.NotFoundException;
import com.kompetencyjny.EventBuddySpring.model.Event;
import com.kompetencyjny.EventBuddySpring.model.Reminder;
import com.kompetencyjny.EventBuddySpring.repo.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final EventService eventService;
    private final PushNotificationService pushNotificationService;

    public Reminder addReminder(Long eventId, String fcmToken, int daysBeforeEvent) {
        Event event = eventService.findByIdInternal(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id: " + eventId + " not found!"));

        Reminder reminder = new Reminder();
        reminder.setFcmToken(fcmToken);
        reminder.setEvent(event);
        reminder.setSent(false);

        reminder.setDaysBeforeEvent(daysBeforeEvent);
        return reminderRepository.save(reminder);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void sendDailyReminders() {
        System.out.println("!!!Sending daily reminders...");
        this.findRemindersToSendToday().forEach(reminder -> {
            pushNotificationService.sendPushNotification(
                    reminder.getFcmToken(),
                    "Przypomnienie o wydarzeniu",
                    "Pamiętaj o wydarzeniu: " + reminder.getEvent().getTitle()+
                            ", które odbędzie się " + reminder.getEvent().getDate().toString(),
                    "event",
                    reminder.getEvent().getId()
            );
            reminder.setSent(true);
            System.out.println("Send");
            reminderRepository.save(reminder);
        });
    }

    public Stream<Reminder> findRemindersToSendToday() {
        LocalDate today = LocalDate.now();
        LocalDate max = today.plusDays(7);
        List<Reminder> candidates = reminderRepository.findCandidates(today, max);
        if (!candidates.isEmpty()) System.out.println("not empty candidates!");

        return candidates.stream()
                .filter(r -> {
                    LocalDate eventDate = r.getEvent().getDate();
                    int daysBefore = r.getDaysBeforeEvent();
                    LocalDate scheduledSendDate = eventDate.minusDays(daysBefore);
                    return !today.isBefore(scheduledSendDate);
                });
    }
}
