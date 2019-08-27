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
import com.avispl.symphony.dal.communicator.SshCommunicator;

import java.io.Closeable;
import java.util.List;

import static com.avispl.symphony.dal.communicator.sample.util.CommunicatorUtils.startSshServer;
import static java.util.Collections.singletonList;
import static org.apache.sshd.common.util.io.IoUtils.closeQuietly;

/**
 * An example of a device that utilizes {@link SshCommunicator} library. <br>
 * This library provides convenient way to execute commands using SSH in context of Symphony framework.
 * It also implements {@link Pingable} and {@link SnmpQueryable} interfaces used by Symphony to collect appropriate device statistics. <br>
 * Note that while this sample only implements {@link Monitorable} interface, depending on device capabilities, there are other interfaces which can be
 * implemented: {@link Controller}, {@link CallController}, {@link MultipointControlUnit}.
 *
 * @author Symphony Dev Team<br> Created on May 8, 2019
 */
public class SshCommunicatorDevice extends SshCommunicator implements Monitorable {

    private static Closeable ssh;
    //port of test server
    private int serverPort = 5333;
    
    public SshCommunicatorDevice() {
    	
    	this.setHost("localhost");
    	this.setPort(serverPort);
        //Set list of error response strings
        this.setCommandSuccessList(singletonList(""));
        //Set list of error response strings
        this.setCommandErrorList(singletonList("ERROR\r\n"));
        //Set list of success login strings
        this.setLoginSuccessList(singletonList(""));
        //login and password to test ssh server
        this.setLogin("test");
		this.setPassword("test");

    }

    @Override
    protected void internalInit() throws Exception {
        //create ssh service for test device on local port 5333
        ssh = startSshServer(serverPort);

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
        closeQuietly(ssh);
        // if needed, code to perform any extra clean up can be put here
    }


    /**
     * {@inheritDoc} <br>
     * This implementation illustrates how to collect monitoring statistics using SSH APIs.
     *
     * @return List of monitoring statistics
     * @throws Exception if any error occurs
     */
    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        //send command via ssh and parse response to get statistics
        String commandResult = send("hostname");
        int bitRateRx = commandResult.length();

        //Collect endpoint statistics
        EndpointStatistics statistics = new EndpointStatistics();
        VideoChannelStats videoChannelStats = new VideoChannelStats();
        videoChannelStats.setBitRateRx(bitRateRx);
        statistics.setVideoChannelStats(videoChannelStats);
        return singletonList(statistics);
    }

    public static void main(String[] args) throws Exception {
        //Create and initialize device.
        SshCommunicatorDevice device = new SshCommunicatorDevice();
        device.setHost("localhost");
        device.setPort(5333);

        //Set list of success response strings
        device.setCommandSuccessList(singletonList(""));
        //Set list of error response strings
        device.setCommandErrorList(singletonList("ERROR\r\n"));
        //Set list of success login strings
        device.setLoginSuccessList(singletonList(""));

        //User name and password
        device.setLogin("test");
        device.setPassword("test");

        device.init();

        // compose corresponding statistics object
        // note that type of statistics object depends on what monitoring information can be obtained from device
        // for generic computer statistics, use GenericStatistics
        // for call/audio/video statistics, use EndpointStatistics
        // for MCU, use MCUStatistics
        // for extended monitorable properties which are not defined in any known Statistics object, use ExtendedStatistics
        EndpointStatistics statistics = (EndpointStatistics) device.getMultipleStatistics().get(0);
        System.out.println("BitRateRx = " + statistics.getVideoChannelStats().getBitRateRx());

        device.destroy();
    }
}
