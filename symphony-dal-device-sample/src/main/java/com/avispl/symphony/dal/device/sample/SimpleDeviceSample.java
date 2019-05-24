/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.device.sample;

import com.avispl.symphony.api.dal.Device;
import com.avispl.symphony.api.dal.Version;
import com.avispl.symphony.api.dal.dto.monitor.AudioChannelStats;
import com.avispl.symphony.api.dal.dto.monitor.CallStats;
import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.VideoChannelStats;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.ping.Pingable;

import java.util.List;
import java.util.Random;

import static java.util.Collections.singletonList;

/**
 * Simple minimalistic DAL device sample with hardcoded/generated values
 * Real adapters are supposed to be communicating with remote devices through network
 *
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class SimpleDeviceSample implements Device, Pingable, Monitorable {

    @Override
    public String getAddress() {
        // IP address / hostname of the device
        return "10.0.0.1";
    }

    @Override
    public Version retrieveSoftwareVersion() throws Exception {
        // version of software / firmware target device is running
        return new Version("1.2.3");
    }

    @Override
    public void init() throws Exception {
        // this method is called after instance is created and
        // JavaBean properties are set
        System.out.print("Initializing device " + SimpleDeviceSample.class);
    }

    @Override
    public boolean isInitialized() {
        // has to return true once adapter is fully initialized
        return true;
    }

    @Override
    public void destroy() {
        // this method is called upon a Symphony shutdown
        // here all persistent resources, sockets have to be released
        System.out.print("Destroying device " + SimpleDeviceSample.class);
    }

    @Override
    public int ping() throws Exception {
        // has to perform ping and return ping latency to a target device
        return new Random().nextInt(50);
    }

    @Override
    public int getPingTimeout() {
        // ping timeout value
        return 100;
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {

        // device adapter may return various statistics
        // see javadoc and classes that inherited from com.avispl.symphony.api.dal.dto.monitor.Statistics
        // following code constructs dummy instance of com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics
        // with fake data. In real adapter following information needs to be retrieved from the target device
        // using remote network call

        EndpointStatistics statistics = new EndpointStatistics();
        statistics.setInCall(true);

        CallStats callStats = new CallStats();
        callStats.setCallRateRx(100);
        callStats.setPercentPacketLossRx(new Random().nextFloat());
        statistics.setCallStats(callStats);

        AudioChannelStats audioChannelStats = new AudioChannelStats();
        audioChannelStats.setJitterRx((float)new Random().nextInt(30));
        statistics.setAudioChannelStats(audioChannelStats);

        VideoChannelStats videoChannelStats = new VideoChannelStats();
        videoChannelStats.setFrameRateRx(2f);
        videoChannelStats.setFrameSizeTx(1200, 700);
        statistics.setVideoChannelStats(videoChannelStats);

        return singletonList(statistics);
    }
}
