package com.avispl.symphony.tal.sample;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.avispl.symphony.api.tal.TalConfigService;
import com.avispl.symphony.api.tal.TalProxy;
import com.avispl.symphony.api.tal.dto.TalTicket;
import com.avispl.symphony.api.tal.dto.UpdateSource;
import com.avispl.symphony.api.tal.error.TalAdapterSyncException;

/**
 * Sample TAL adapter implementation.
 * Subscribes itself after application initialization for ticket updates of {@link #ADAPTER_TYPE} sync type
 */
public class SampleTalAdapterImpl implements SampleTalAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SampleTalAdapterImpl.class);

    private static final String ADAPTER_TYPE = "SampleAdapter";

    @Autowired
    private InMemoryTicketSystem ticketSystem;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TalProxy talProxy;

    @Autowired
    private TalConfigService talConfigService;

    /**
     * Subscribes the adapter for updates from Symphony
     */
    public void init() {
        logger.info("Initializing Sample TAL adapter");
        talProxy.subscribeUpdates(ADAPTER_TYPE, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TalTicket syncTalTicket(TalTicket talTicket, boolean confirmationNeeded) throws TalAdapterSyncException {
        try {
            validateTicket(talTicket);

            TalTicket mappedTicket = ticketMapper.mapFromSymphonyTicket(talTicket);

            return ticketSystem.handleTicket(mappedTicket);
        } catch (Exception e) {
            logger.warn("Failed to sync ticket from TAL to InMemory Ticket System {}", talTicket);
            throw new TalAdapterSyncException("Cannot sync TAL ticket", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncTicketToSymphony(TalTicket thirdPartyTicket, UUID accountId) {
        try {
            validateTicket(thirdPartyTicket);

            TalTicket mappedTicket = ticketMapper.mapToSymphonyTicket(thirdPartyTicket, accountId);

            talProxy.pushUpdatesToTal(mappedTicket);
        } catch (Exception e) {
            throw new RuntimeException("Cannot sync a ticket from InMemory Ticket System", e);
        }
    }

    private void validateTicket(TalTicket ticket) {
        try {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket is null");
            }

            UUID customerId = UUID.fromString(ticket.getCustomerId());

            String syncType = talConfigService.getCustomerSyncType(customerId, UpdateSource.THIRD_PARTY);

            if (!ADAPTER_TYPE.equalsIgnoreCase(syncType)) {
                logger.warn("Unsupported sync type = {}", syncType);
                throw new IllegalStateException("Unsupported sync type = " + syncType);
            }
        } catch (Exception e) {
            logger.warn("Failed to validate ticket {}", ticket);
            throw new RuntimeException("Failed to validate ticket", e);
        }
    }

}
