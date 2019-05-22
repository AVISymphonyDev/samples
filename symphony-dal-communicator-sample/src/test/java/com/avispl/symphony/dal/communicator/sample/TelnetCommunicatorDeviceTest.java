/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.sample;

import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;

public class TelnetCommunicatorDeviceTest {

    @Test
    public void shouldGetStatistic() throws Exception {
        TelnetCommunicatorDevice device = new TelnetCommunicatorDevice();
        device.setHost("localhost");
        device.setPort(5334);
        device.setCommandSuccessList(singletonList(""));
        device.setCommandErrorList(singletonList("ERROR\r\n"));
        device.setLoginSuccessList(singletonList(""));

        device.init();

        List<Statistics> statistics = device.getMultipleStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertEquals(1, statistics.size());
        Assert.assertNotNull(((EndpointStatistics) statistics.get(0)).getVideoChannelStats().getBitRateRx());
    }
}
