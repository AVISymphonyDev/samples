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

public class SshComunicatorDeviceTest {

    @Test
    public void shouldGetStatistic() throws Exception {
        SshCommunicatorDevice device = new SshCommunicatorDevice();
        device.setHost("localhost");
        device.setPort(5333);
        device.setCommandSuccessList(singletonList(""));
        device.setCommandErrorList(singletonList("ERROR\r\n"));
        device.setLoginSuccessList(singletonList(""));
        device.setLogin("test");
        device.setPassword("test");
        device.init();

        List<Statistics> statistics = device.getMultipleStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertEquals(1, statistics.size());
        Assert.assertNotNull(((EndpointStatistics) statistics.get(0)).getVideoChannelStats().getBitRateRx());
    }
}
