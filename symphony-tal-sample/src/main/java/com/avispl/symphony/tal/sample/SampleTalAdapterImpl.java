/*
 * Copyright (c) 2019 AVI-SPL Inc. All Rights Reserved.
 */

package com.avispl.symphony.tal.sample;

import java.util.UUID;

import com.avispl.symphony.api.tal.TalAdapter;
import com.avispl.symphony.api.tal.dto.TicketSystemConfig;
import com.avispl.symphony.tal.mocks.MockTalConfigService;
import com.avispl.symphony.tal.mocks.MockTalProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avispl.symphony.api.tal.TalConfigService;
import com.avispl.symphony.api.tal.TalProxy;
import com.avispl.symphony.api.tal.dto.TalTicket;
import com.avispl.symphony.api.tal.error.TalAdapterSyncException;

/**
 * Sample TAL adapter implementation.
 *
 * @author Symphony Dev Team<br> Created on 7 Dec 2018
 * @since 4.6
 */
public class SampleTalAdapterImpl implements TalAdapter {

    /**
     * Logger instance
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Instance of a TalConfigService, set by Symphony via {@link #setTalConfigService(TalConfigService)}
     * In sake of testing simplicity, one may use MockTalConfigService provided with this sample
     */
    private TalConfigService talConfigService;

    /**
     * Instance of a TalProxy, set by Symphony via {@link #setTalProxy(TalProxy)}
     * In sake of testing simplicity, one may use MockTalProxy provided with this sample
     */
    private TalProxy talProxy;

    /**
     * Instance of TicketSystemConfig that contains mappings and destination
     * ticketing system configuration
     */
    private TicketSystemConfig config;

    /**
     * Account identifier - have to be provided to 3rd party adapter implementors by Symphony team
     */
    private UUID accountId = UUID.fromString("e8ab4178-81fb-43c9-8eae-1a61d609a991");

    /**
     * Default no-arg constructor
     */
    public SampleTalAdapterImpl() {
        /**
         * Uncomment following in order to use mocks instead of setter-injected objects
         * Warning: use for development purposes only!
         */
        // this.talConfigService = new MockTalConfigService();
        // this.talProxy = new MockTalProxy();
    }

    /**
     * Called by Symphony automatically after instance of adapter is created and talConfigService/talProxy setters
     *
     * Important: In this method developer must not perform any heavy synchronous initialization or I/O bound operations.
     * All such operations must be performed asynchronously in background thread(s).
     */
    @Override
    public void init() {
        logger.info("Initializing Sample TAL adapter");

        // In order to get ticket updates from Symphony adapter must subscribe to this explicitly here
        // After subscription is done, all updates will come to this adapter instance via calls to syncTalTicket method
        talProxy.subscribeUpdates(accountId, this);

        try {
            // obtain adapter configuration
            setConfig(talConfigService.retrieveTicketSystemConfig(accountId));
        } catch (Exception e) {
            throw new RuntimeException("SampleTalAdapterImpl was unable to retrieve " +
                    "configuration from TalConfigService: " + e.getMessage(), e);
        }

        // subscribe for getting adapter configuration updates
        talConfigService.subscribeForTicketSystemConfigUpdate(accountId,
                (ticketSystemConfig) -> setConfig(ticketSystemConfig));
    }

    /**
     * Called by Symphony when application is about to exit
     */
    @Override
    public void destroy() {
        // destroy any persistent resources
        // such as thread pools or persistent connections
    }

    /**
     * Invoked on each ticket update that happens in Symphony
     * @param talTicket instance of ticket that contains updated data. Ticket always come containing all fields even those that didn't change
     * @return instance of TalTicket that contains thirdPartyId and thirdPartyLink set for ticket, comments and attachments provisioned in 3rd party system
     * @throws TalAdapterSyncException
     */
    @Override
    public TalTicket syncTalTicket(TalTicket talTicket) throws TalAdapterSyncException {
        try {
            // map status, priorities, users to comply with 3rd party ticketing system
            TicketMapper.mapSymphonyToThirdParty(talTicket, config);

            // 1. make call to 3rd party ticketing system
            // 2. handle response from 3rd party ticketing system
            // 3. if succeeded change talTicket, set thirdPartyId and thirdPartyLink set for ticket, comments and attachments provisioned in 3rd party system
            // 4. return updated instance using "return statement" to the caller

            return talTicket;

        } catch (Exception e) {
            logger.warn("Failed to sync ticket from TAL to InMemory Ticket System {}", talTicket);
            throw new TalAdapterSyncException("Cannot sync TAL ticket", e);
        }
    }

    public void setTalConfigService(TalConfigService talConfigService) {
        this.talConfigService = talConfigService;
    }

    public TalConfigService getTalConfigService() {
        return this.talConfigService;
    }

    public void setTalProxy(TalProxy talProxy) {
        this.talProxy = talProxy;
    }

    public TalProxy getTalProxy() {
        return this.talProxy;
    }

    public TicketSystemConfig getConfig() {
        return config;
    }

    public void setConfig(TicketSystemConfig config) {
        this.config = config;
    }
}
