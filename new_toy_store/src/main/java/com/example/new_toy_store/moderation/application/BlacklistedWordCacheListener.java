package com.example.new_toy_store.moderation.application;

import com.example.new_toy_store.global.event.BlacklistedWordCreatedEvent;
import com.example.new_toy_store.global.event.BlacklistedWordDeletedEvent;
import com.example.new_toy_store.global.event.BlacklistedWordRestoredEvent;
import com.example.new_toy_store.global.event.BlacklistedWordUpdatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BlacklistedWordCacheListener {

    private final BlacklistedWordCache cache;

    public BlacklistedWordCacheListener(BlacklistedWordCache cache) {
        this.cache = cache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(BlacklistedWordCreatedEvent event) {
        cache.add(event.payload().word());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpdated(BlacklistedWordUpdatedEvent event) {
        cache.replace(event.previous().word(), event.current().word());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(BlacklistedWordDeletedEvent event) {
        cache.remove(event.payload().word());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRestored(BlacklistedWordRestoredEvent event) {
        cache.add(event.payload().word());
    }
}
