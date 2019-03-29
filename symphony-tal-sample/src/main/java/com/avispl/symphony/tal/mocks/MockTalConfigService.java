/*
 * Copyright (c) 2019 AVI-SPL Inc. All Rights Reserved.
 */

package com.avispl.symphony.tal.mocks;

import com.avispl.symphony.api.tal.TalConfigService;
import com.avispl.symphony.api.tal.dto.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Mock implementation of TalConfigService
 * This mock could be used to simplify adapter testing having parts of Symphony infrastructure
 *
 * @author Symphony Dev Team<br> Created on Jan 20, 2019
 */
public class MockTalConfigService implements TalConfigService {

    @Override
    public TicketSystemConfig retrieveTicketSystemConfig(UUID uuid) throws ExecutionException {
        TicketSystemConfig config = new TicketSystemConfig();

        Map<String, String> instanceConfigMapping = new HashMap<>();
        instanceConfigMapping.put(TicketSourceConfigProperty.URL, "http://instance.io");
        instanceConfigMapping.put(TicketSourceConfigProperty.API_PATH, "/api/ticket");
        instanceConfigMapping.put(TicketSourceConfigProperty.LOGIN, "jsmith");
        instanceConfigMapping.put(TicketSourceConfigProperty.PASSWORD, "Df764!dfp");

        // keys are Symphony priorities and values are third party mapped statuses
        Map<String, String> customerPriorityMappingForThirdParty  = new HashMap<>();
        customerPriorityMappingForThirdParty.put("Critical", "10");
        customerPriorityMappingForThirdParty.put("Major", "5");
        customerPriorityMappingForThirdParty.put("Minor", "3");
        customerPriorityMappingForThirdParty.put("Informational", "1");

        // keys are third party priorities and values are Symphony mapped statuses
        Map<String, String> customerPriorityMappingForSymphony  = new HashMap<>();
        customerPriorityMappingForThirdParty.put("10", "Critical");
        customerPriorityMappingForThirdParty.put("5", "Major");
        customerPriorityMappingForThirdParty.put("3", "Minor");
        customerPriorityMappingForThirdParty.put("1", "Informational");

        // keys are third party users and values are Symphony mapped user
        Map<String, String> userMappingForSymphony = new HashMap<>();
        userMappingForSymphony.put("jdoe", "john.doe@acme.com");
        userMappingForSymphony.put("psmith", "peter.smith@acme.com");

        // keys are Symphony users and values are third party mapped users
        Map<String, UserIdMapping> userMappingForThirdParty = new HashMap<>();
        userMappingForThirdParty.put("john.doe@acme.com", new UserIdMapping("jdoe", "username"));
        userMappingForThirdParty.put("peter.smith@acme.com", new UserIdMapping("psmith", "username"));

        // keys are third party statuses and values are Symphony mapped statuses
        Map<String, String> statusMappingForSymphony = new HashMap<>();
        statusMappingForSymphony.put("New", "Open");
        statusMappingForSymphony.put("In progress", "Open");
        statusMappingForSymphony.put("On hold", "Open");
        statusMappingForSymphony.put("Canceled", "Close");
        statusMappingForSymphony.put("Resolved", "Close");
        statusMappingForSymphony.put("Closed", "Close");

        // keys are Symphony statuses and values are third party mapped statuses
        Map<String, String> statusMappingForThirdParty = new HashMap<>();
        statusMappingForThirdParty.put("Open", "In progress");
        statusMappingForThirdParty.put("Close", "Closed");
        statusMappingForThirdParty.put("ClosePending", "Resolved");

        config.setPriorityMappingForThirdParty(customerPriorityMappingForThirdParty);
        config.setPriorityMappingForSymphony(customerPriorityMappingForSymphony);
        config.setUserMappingForSymphony(userMappingForSymphony);
        config.setUserMappingForThirdParty(userMappingForThirdParty);
        config.setTicketSourceConfig(instanceConfigMapping);
        config.setStatusMappingForSymphony(statusMappingForSymphony);
        config.setStatusMappingForThirdParty(statusMappingForThirdParty);

        return config;
    }

    @Override
    public void subscribeForTicketSystemConfigUpdate(UUID uuid, Consumer<TicketSystemConfig> consumer) {
        // no op
    }
}
