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

public class RestCommunicatorDeviceTest {

    private RestCommunicatorDevice device;

    @Before
    public void setUp() throws Exception {
        device = new RestCommunicatorDevice();
        device.setProtocol("https");
        device.setHost("worldclockapi.com");
        device.setBaseUri("api/json");
        device.setTrustAllCertificates(true);
        device.init();
    }

    @Test
    public void shouldGetStatistic() throws Exception {
        List<Statistics> statistics = device.getMultipleStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertEquals(1, statistics.size());
        Assert.assertNotNull(((EndpointStatistics) statistics.get(0)).getVideoChannelStats().getBitRateRx());
    }
}
