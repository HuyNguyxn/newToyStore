package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.NotificationEmailRequestedEvent;
import com.example.new_toy_store.infrastructure.mail.MailService;
import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.application.dto.response.NotificationRecipientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEmailEventListener.class);

    private final MailService mailService;
    private final UserFacade userFacade;

    public NotificationEmailEventListener(MailService mailService, UserFacade userFacade) {
        this.mailService = mailService;
        this.userFacade = userFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendNotificationEmail(NotificationEmailRequestedEvent event) {
        try {
            NotificationRecipientResponse recipient = userFacade.getNotificationRecipient(event.recipientUserId());
            mailService.sendEmail(recipient.email(), "[NewToyStore] " + event.title(), event.message());
        } catch (RuntimeException ex) {
            log.warn("Failed to send notification email. notificationId={}, userId={}",
                    event.notificationId(), event.recipientUserId(), ex);
        }
    }
}
