package com.avispl.symphony.tal.sample;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.avispl.symphony.api.tal.dto.TalTicket;

/**
 * Simple in-memory ticket system implementation.
 * The system handles incoming tickets and initiates eventual updates of them.
 */
public class InMemoryTicketSystem {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryTicketSystem.class);

    private static final String TICKET_SYSTEM_URL = "https://somewhere/tickets/";
    private static final List<String> PRIORITIES = Arrays.asList("Critical", "Major", "Minor", "Informational");
    private static final List<String> STATUSES = Arrays.asList("Open", "In progress", "On hold", "Resolved", "Closed", "Canceled");
    private static final int MAX_PAUSE_BEFORE_UPDATE = 180000; // 3 min

    @Autowired
    private SampleTalAdapter talAdapter;

    private List<TalTicket> tickets;

    /**
     * Handles tickets received from Symphony. Sets the ticket identifier and link to acce
     *
     * @param ticket new or updated ticket
     * @return the ticket with third-party identifier and link
     */
    public TalTicket handleTicket(TalTicket ticket) {
        // set identifier and link to the ticket in the system
        String ticketId = generateTicketId();
        ticket.setThirdPartyId(ticketId);
        ticket.setThirdPartyLink(TICKET_SYSTEM_URL + ticketId);

        tickets.add(ticket);

        // just to trigger backward sync
        initiateTicketUpdate(ticket);

        return ticket;
    }

    /**
     * Initiates eventual ticket update in the system.
     * The update is being initiated after a pause of duration < {@link #MAX_PAUSE_BEFORE_UPDATE}
     */
    public void initiateTicketUpdate(TalTicket ticket) {
        logger.info("Initiating eventual ticket update");
        CompletableFuture.runAsync(() -> {
            while (waitBeforeUpdate()) {
                UUID symphonyCustomerId = UUID.fromString(ticket.getCustomerId());
                updateTicket(ticket);
                talAdapter.syncTicketToSymphony(ticket, symphonyCustomerId);
            }
        });
    }

    private static boolean waitBeforeUpdate() {
        try {
            Thread.sleep(RandomUtils.nextInt(MAX_PAUSE_BEFORE_UPDATE));
            return true;
        } catch (InterruptedException e) {
            logger.info("Waiting before the update was interrupted", e);
            return false;
        }
    }

    private static String randomPriority() {
        int i = RandomUtils.nextInt(PRIORITIES.size());
        return PRIORITIES.get(i);
    }

    private static String randomStatus() {
        int i = RandomUtils.nextInt(STATUSES.size());
        return STATUSES.get(i);
    }

    private static String generateTicketId() {
        return UUID.randomUUID().toString();
    }

    private static void updateTicket(TalTicket ticket) {
        ticket.setPriority(randomPriority());
        ticket.setStatus(randomStatus());
        ticket.setLastModified(System.currentTimeMillis());
    }

}
