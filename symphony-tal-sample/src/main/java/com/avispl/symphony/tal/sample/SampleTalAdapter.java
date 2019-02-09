package com.avispl.symphony.tal.sample;

import java.util.UUID;

import com.avispl.symphony.api.tal.TalAdapter;
import com.avispl.symphony.api.tal.dto.TalTicket;

/**
 * Extends TAL adapter interface providing possibility to sync a ticket to Symphony.
 */
public interface SampleTalAdapter extends TalAdapter {

    /**
     * Syncs a ticket update from a third-party ticket solution system into Symphony.
     * <br><br>
     * Note, that the {@link TalTicket} model is used just to simplify the sample,
     * in a real system the class representing its tickets should be used here.
     *
     * @param thirdPartyTicket 3rd system ticket
     * @param accountId        Symphony account identifier
     */
    void syncTicketToSymphony(TalTicket thirdPartyTicket, UUID accountId);

}
