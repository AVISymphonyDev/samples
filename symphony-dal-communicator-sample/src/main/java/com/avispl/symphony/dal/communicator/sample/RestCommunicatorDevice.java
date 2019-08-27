/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.sample;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.control.call.CallController;
import com.avispl.symphony.api.dal.control.mcu.MultipointControlUnit;
import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.VideoChannelStats;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.ping.Pingable;
import com.avispl.symphony.api.dal.snmp.SnmpQueryable;
import com.avispl.symphony.dal.communicator.RestCommunicator;

import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.abs;
import static java.util.Collections.singletonList;


/**
 * An example of a device that utilizes {@link RestCommunicator} library. <br>
 * This library provides convenient way to execute GET, POST, PATCH and DELETE HTTP requests and then marchall response to java object in context of Symphony framework.
 * It also implements {@link Pingable} and {@link SnmpQueryable} interfaces used by Symphony to collect appropriate device statistics. <br>
 * Note that while this sample only implements {@link Monitorable} interface, depending on device capabilities, there are other interfaces which can be
 * implemented: {@link Controller}, {@link CallController}, {@link MultipointControlUnit}.
 *
 * @author Symphony Dev Team<br> Created on May 8, 2019
 */
public class RestCommunicatorDevice extends RestCommunicator implements Monitorable {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void authenticate() {
        // not needed when Basic authentication is used (default authentication method)
        // if required for specific device type, code to perform authentication request can be put here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalInit() throws Exception {
        super.internalInit();

        // if needed, code to perform any extra initialization can be put here

        // also this method can be used to validate that device object is fully configured before use
        // e.g. if any device properties are required by this object, code here can validate that they were set with valid values
        // note that HttpCommunicator library already validates host, port, protocol, authenticationScheme and timeout properties

        // note also that exception thrown by this method will prevent object from being initialized and used
    }


    @Override
    protected void internalDestroy() {
        super.internalDestroy();

        // if needed, code to perform any extra clean up can be put here
    }

    /**
     * {@inheritDoc} <br>
     * This implementation illustrates how to collect monitoring statistics using REST APIs.
     *
     * @return List of monitoring statistics
     * @throws Exception if any error occurs
     */
    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        //call rest method and convert response to java objects
        Map<?,?> data = doGet("utc/now", Map.class);
        int bitRateRx = abs(((Long) data.get("currentFileTime")).intValue());

        // compose corresponding statistics object
        // note that type of statistics object depends on what monitoring information can be obtained from device
        // for generic computer statistics, use GenericStatistics
        // for call/audio/video statistics, use EndpointStatistics
        // for MCU, use MCUStatistics
        // for extended monitorable properties which are not defined in any known Statistics object, use ExtendedStatistics
        EndpointStatistics statistics = new EndpointStatistics();
        VideoChannelStats videoChannelStats = new VideoChannelStats();
        videoChannelStats.setBitRateRx(bitRateRx);
        statistics.setVideoChannelStats(videoChannelStats);

        return singletonList(statistics);
    }

    public static void main(String[] args) throws Exception {
        //Create and initialize device.
        RestCommunicatorDevice device = new RestCommunicatorDevice();
        device.setProtocol("https");
        device.setHost("worldclockapi.com");
        device.setBaseUri("api/json");
        device.setTrustAllCertificates(true);
        device.init();

        //Collect device statistics
        EndpointStatistics statistics = (EndpointStatistics) device.getMultipleStatistics().get(0);
        System.out.println("BitRateRx = " + statistics.getVideoChannelStats().getBitRateRx());

        device.destroy();
    }
}
