/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.sample;

import com.avispl.symphony.api.dal.dto.monitor.AudioChannelStats;
import com.avispl.symphony.api.dal.dto.monitor.CallStats;
import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.VideoChannelStats;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.BaseDevice;

import java.util.List;
import java.util.Random;

import static java.util.Collections.singletonList;

/**
 * Simple minimalistic DAL device sample with hardcoded/generated values
 * Real adapters are supposed to be communicating with remote devices through network
 *
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class SampleBaseDevice extends BaseDevice implements Monitorable {

    /**
     * See base class {@link BaseDevice} for properties:
     * - {@link #setHost(String)}, {@link #getHost()} etc
     */

    @Override
    protected void internalInit() throws Exception {
        // BaseDevice has Device.init() method implemented.
        // In case DAL adapter developer decides to implement custom initialization routine it could be done just in internalInit() only
        // This method is called once JavaBean properties are set
        // perform initialization of device adapter here
        super.internalInit();
    }

    protected void internalDestroy() {
        // BaseDevice has Device.destroy() method implemented.
        // In case DAL adapter developer decides to implement custom destroy routine it could be done just in internalDestroy() only
        // This method is called upon a Symphony shutdown
        // here all persistent resources, sockets have to be released
        super.internalDestroy();
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
