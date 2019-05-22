/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.sample;

import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class HttpCommunicatorDeviceTest {

    private HttpCommunicatorDevice device;

    @Before
    public void setUp() throws Exception {
        device = new HttpCommunicatorDevice();
        device.setProtocol("https");
        device.setHost("blockchain.info");
        device.init();
    }

    @Test
    public void shouldPingHost() throws Exception {
        int ping = device.ping();
        Assert.assertNotEquals(0, ping);
    }

    @Test
    public void shouldGetStatistic() throws Exception {
        List<Statistics> statistics = device.getMultipleStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertEquals(1, statistics.size());
        Assert.assertNotNull(((EndpointStatistics) statistics.get(0)).getVideoChannelStats().getFrameRateRx());
    }
}
