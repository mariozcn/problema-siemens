package org.hexview.trainticketing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default {@link EmailService} that logs the message instead of sending it.
 * Also keeps a small in-memory outbox so tests can assert what was "sent".
 */
@Service
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    private final List<SentEmail> outbox = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void send(String toAddress, String subject, String body) {
        log.info("[EMAIL] to={} | subject={}\n{}", toAddress, subject, body);
        outbox.add(new SentEmail(toAddress, subject, body));
    }

    public List<SentEmail> getOutbox() {
        synchronized (outbox) {
            return List.copyOf(outbox);
        }
    }

    public void clearOutbox() {
        outbox.clear();
    }

    public record SentEmail(String to, String subject, String body) {
    }
}
