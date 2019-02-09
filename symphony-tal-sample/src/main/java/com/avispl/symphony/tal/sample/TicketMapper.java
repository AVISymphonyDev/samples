package com.avispl.symphony.tal.sample;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.avispl.symphony.api.tal.TalConfigService;
import com.avispl.symphony.api.tal.dto.TalTicket;
import com.avispl.symphony.api.tal.dto.TicketSystemConfig;

/**
 * Performs mapping of TAL ticket into third-party one and vice-versa
 */
public class TicketMapper {

    @Autowired
    private TalConfigService talConfigService;

    private final Map<UUID, TicketSystemConfig> ticketSystemConfigs = new ConcurrentHashMap<>();

    /**
     * Converts a TAL ticket into appropriate representation for a Ticket System
     * and performing statuses/priorities/etc mapping.
     * <br><br>
     * Note that the {@link TalTicket} model is used for both TAL and third-party tickets just to simplify the sample,
     * when integrating with a real ticket system, the appropriate class for third-party tickets should be used.
     *
     * @param talTicket TAL ticket to be mapped into third-party's one
     * @return the mapped ticket
     */
    public TalTicket mapFromSymphonyTicket(TalTicket talTicket)  {
        UUID accountId = UUID.fromString(talTicket.getCustomerId());
        String thirdPartyStatus = mapSymphonyStatus(talTicket.getStatus(), accountId);
        talTicket.setStatus(thirdPartyStatus);
        return talTicket;
    }

    /**
     * Converts a third-party ticket into TAL ticket setting the Symphony specific fields
     * and performing statuses/priorities/etc mapping.
     * <br><br>
     * Note that the {@link TalTicket} model is used for both TAL and third-party tickets just to simplify the sample,
     * when integrating with a real ticket system, the appropriate class for third-party tickets should be used.
     *
     * @param thirdPartyTicket a third-party ticket to be mapped into Symphony's one
     * @param accountId        the customer account identifier on Symphony
     * @return the mapped ticket
     */
    public TalTicket mapToSymphonyTicket(TalTicket thirdPartyTicket, UUID accountId) {
        // set Symphony account id
        thirdPartyTicket.setCustomerId(accountId.toString());

        // set an appropriate Symphony status
        String status = thirdPartyTicket.getStatus();
        String symphonyStatus = mapToSymphonyStatus(status, accountId);
        thirdPartyTicket.setStatus(symphonyStatus);

        return thirdPartyTicket;
    }

    private TicketSystemConfig getTicketSystemConfig(UUID customerId) {
        return ticketSystemConfigs.computeIfAbsent(customerId, id -> {
            try {
                return talConfigService.retrieveTicketSystemConfig(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String mapToSymphonyStatus(String thirdPartyStatus, UUID accountId) {
        // retrieve mappings for symphony to third-party statuses
        Map<String, String> statusMapping = getTicketSystemConfig(accountId)
                .getStatusMappingForSymphony();

        return statusMapping.get(thirdPartyStatus);
    }

    private String mapSymphonyStatus(String symphonyStatus, UUID accountId) {
        // retrieve mappings for third-party to symphony statuses
        Map<String, String> statusMapping = getTicketSystemConfig(accountId)
                .getStatusMappingForThirdParty();

        return statusMapping.get(symphonyStatus);
    }

}
