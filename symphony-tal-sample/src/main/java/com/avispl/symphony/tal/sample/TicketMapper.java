/*
 * Copyright (c) 2019 AVI-SPL Inc. All Rights Reserved.
 */

package com.avispl.symphony.tal.sample;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.avispl.symphony.api.tal.TalConfigService;
import com.avispl.symphony.api.tal.dto.TalTicket;
import com.avispl.symphony.api.tal.dto.TicketSystemConfig;

/**
 * Performs mapping of TAL ticket into third-party one and vice-versa
 *
 * @author Symphony Dev Team<br> Created on Dec 7, 2018
 * @since 4.6
 */
public class TicketMapper {

    /**
     * Converts a TAL ticket into appropriate representation for a Ticket System
     * and performs statuses/priorities/etc mapping.
     *
     * Note that the {@link TalTicket} model is used for both TAL and third-party tickets just to simplify the sample,
     * when integrating with a real ticket system, the appropriate class for third-party tickets should be used.
     *
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     * @return the mapped ticket
     */
    public static TalTicket mapSymphonyToThirdParty(TalTicket ticket, TicketSystemConfig config)
    {
        mapTicketStatus(ticket, config);
        mapTicketPriority(ticket, config);
        mapRequestor(ticket, config);
        mapAssignee(ticket, config);
        mapCommentCreator(ticket, config);
        mapAttachmentCreator(ticket, config);

        return ticket;
    }

    /**
     * Maps ticket status from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapTicketStatus(TalTicket ticket, TicketSystemConfig config) {
        String thirdPartyStatus = config.getStatusMappingForThirdParty().get(ticket.getStatus());

        if (thirdPartyStatus == null)
            return;

        ticket.setStatus(thirdPartyStatus);
    }

    /**
     * Maps ticket priority from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapTicketPriority(TalTicket ticket, TicketSystemConfig config) {
        String thirdPartyPriority = config.getPriorityMappingForThirdParty().get(ticket.getPriority());

        if (thirdPartyPriority == null)
            return;

        ticket.setPriority(thirdPartyPriority);
    }

    /**
     * Maps ticket requestor from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapRequestor(TalTicket ticket, TicketSystemConfig config) {
        ticket.setRequester(mapUser(ticket.getRequester(), config));
    }

    /**
     * Maps ticket assignee from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapAssignee(TalTicket ticket, TicketSystemConfig config) {
        ticket.setAssignedTo(mapUser(ticket.getAssignedTo(), config));
    }

    /**
     * Maps comment requestors from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapCommentCreator(TalTicket ticket, TicketSystemConfig config) {
        Optional.ofNullable(ticket.getComments())
                .orElse(Collections.emptySet())
                .stream()
                .forEach(c -> c.setCreator(mapUser(c.getCreator(), config)));
    }

    /**
     * Maps attachment requestors from Symphony to 3rd party ticketing system
     * @param ticket ticket instance that needs to be mapped
     * @param config adapter configuration
     */
    private static void mapAttachmentCreator(TalTicket ticket, TicketSystemConfig config) {
        Optional.ofNullable(ticket.getAttachments())
                .orElse(Collections.emptySet())
                .stream()
                .forEach(c -> c.setCreator(mapUser(c.getCreator(), config)));
    }

    /**
     * Maps user ID from Symphony to 3rd party ticketing system
     * @param userId user identifier to map
     * @param config adapter configuration
     * @return mapped identifier eligible for 3rd party ticketing system
     */
    private static String mapUser(String userId, TicketSystemConfig config) {
        if (userId == null)
            return null;

        String thirdPartyUserId = config.getUserMappingForThirdParty().get(userId).getThirdPartyId();

        if (thirdPartyUserId == null)
            return userId;

        return thirdPartyUserId;
    }
}
