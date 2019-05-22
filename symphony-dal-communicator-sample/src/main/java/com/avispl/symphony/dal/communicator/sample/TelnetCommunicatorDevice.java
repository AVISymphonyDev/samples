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
import com.avispl.symphony.dal.communicator.TelnetCommunicator;

import java.io.Closeable;
import java.util.List;

import static com.avispl.symphony.dal.communicator.sample.util.CommunicatorUtils.startTelnetServer;
import static java.util.Collections.singletonList;
import static org.apache.sshd.common.util.io.IoUtils.closeQuietly;

/**
 * An example of a device that utilizes {@link TelnetCommunicator} library. <br>
 * This library provides convenient way to execute commands using TELNET in context of Symphony framework.
 * It also implements {@link Pingable} and {@link SnmpQueryable} interfaces used by Symphony to collect appropriate device statistics. <br>
 * Note that while this sample only implements {@link Monitorable} interface, depending on device capabilities, there are other interfaces which can be
 * implemented: {@link Controller}, {@link CallController}, {@link MultipointControlUnit}.
 */
public class TelnetCommunicatorDevice extends TelnetCommunicator implements Monitorable {

    private Closeable telnet;

    @Override
    protected void internalInit() throws Exception {
        //create telnet service for test device on local port 5334
        telnet = startTelnetServer(5334);


        // if needed, code to perform any extra initialization can be put here

        // also this method can be used to validate that device object is fully configured before use
        // e.g. if any device properties are required by this object, code here can validate that they were set with valid values
        // note that HttpCommunicator library already validates host, port, protocol, authenticationScheme and timeout properties

        // note also that exception thrown by this method will prevent object from being initialized and used
        super.internalInit();
    }

    @Override
    protected void internalDestroy() {
        super.internalDestroy();

        //close telnet server on device destroy
        closeQuietly(telnet);

        // if needed, code to perform any extra clean up can be put here
    }

    /**
     * {@inheritDoc} <br>
     * This implementation illustrates how to collect monitoring statistics using TELNET APIs.
     *
     * @return monitoring statistics
     * @throws Exception if any error occurs
     */
    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        //Send command via telnet and parse response to get statistics.
        String date = send("cmd /c systeminfo");
        int bitRateRx = Integer.valueOf(date.replaceAll("[^\\d]", "").substring(0, 3));

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
        TelnetCommunicatorDevice device = new TelnetCommunicatorDevice();
        device.setHost("localhost");
        device.setPort(5334);

        //Set list of success response strings
        device.setCommandSuccessList(singletonList(""));
        //Set list of error response strings
        device.setCommandErrorList(singletonList("ERROR\r\n"));
        //Set list of success login strings
        device.setLoginSuccessList(singletonList(""));

        device.init();

        //Collect device statistics
        EndpointStatistics statistics = (EndpointStatistics) device.getMultipleStatistics().get(0);
        System.out.println("BitRateRx = " + statistics.getVideoChannelStats().getBitRateRx());

        device.destroy();
    }
}
