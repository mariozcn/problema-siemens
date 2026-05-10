package org.hexview.trainticketing.service;

public interface EmailService {

    void send(String toAddress, String subject, String body);
}
