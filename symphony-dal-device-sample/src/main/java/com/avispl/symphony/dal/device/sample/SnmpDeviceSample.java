/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.device.sample;

import com.avispl.symphony.api.dal.Device;
import com.avispl.symphony.api.dal.Version;
import com.avispl.symphony.api.dal.dto.monitor.McuPorts;
import com.avispl.symphony.api.dal.dto.monitor.McuStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.snmp.SnmpEntry;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.ping.Pingable;
import com.avispl.symphony.api.dal.snmp.SnmpQueryable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple minimalistic DAL device sample with hardcoded/generated values with SNMP capabilities
 * Real adapters are supposed to be communicating with remote devices through network
 *
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class SnmpDeviceSample implements Device, Pingable, Monitorable, SnmpQueryable {

    @Override
    public String getAddress() {
        // IP address / hostname of the device
        return "10.0.0.2";
    }

    public Version retrieveSoftwareVersion() throws Exception {
        // version of software / firmware target device is running
        return new Version("1.10.7");
    }

    public void init() throws Exception {
        System.out.print("Initializing device with SnmpQuerable capabilities " + SnmpDeviceSample.class + ". Exported OIDs: 1.3.6.1.4.1.9.1.0, 1.3.6.1.4.1.9.1.1, 1.3.6.1.4.1.9.1.2");
    }

    public boolean isInitialized() {
        return true;
    }

    public void destroy() {
        System.out.print("Destroying device " + SnmpDeviceSample.class);
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        // statistics
        McuStatistics statistics = new McuStatistics();

        // audio port
        McuPorts audio = new McuPorts();
        audio.setTotal(255);
        audio.setOccupied(35);
        audio.setReserved(15);
        audio.setFree(205);

        statistics.setAudioMcuPorts(audio);

        // video
        McuPorts video = new McuPorts();
        video.setTotal(255);
        video.setOccupied(35);
        video.setReserved(15);
        video.setFree(205);
        statistics.setVideoMcuPorts(video);

        return Collections.singletonList(statistics);
    }

    public int ping() throws Exception {
        return new Random().nextInt(50);
    }

    public int getPingTimeout() {
        return 200;
    }

    public Collection<SnmpEntry> querySnmp(Collection<String> snmpOids) throws Exception {

        if (snmpOids == null) {
            return Collections.emptyList();
        }

        List<SnmpEntry> snmpData = new ArrayList<SnmpEntry>();
        if (snmpOids.contains("1.3.6.1.4.1.9.1.0")) {
            snmpData.add(new SnmpEntry("1.3.6.1.4.1.9.1.0", "ProductID", "TestProductName"));
        }

        if (snmpOids.contains("1.3.6.1.4.1.9.1.1")) {

            float minCpu = 20.0f, maxCpu = 100.0f;
            float cpuLoad = new Random().nextFloat() * (maxCpu - minCpu) + minCpu;

            snmpData.add(new SnmpEntry("1.3.6.1.4.1.9.1.1", "CPULoad", Float.toString(cpuLoad)));
        }

        if (snmpOids.contains("1.3.6.1.4.1.9.1.2")) {

            int minRam = 1243523452, maxRam = 1245157120;
            int freeRam = (int) (Math.random() * ((maxRam - minRam) + 1)) + minRam;

            snmpData.add(new SnmpEntry("1.3.6.1.4.1.9.1.2", "FreeRAM", Integer.toString(freeRam)));
        }

        return snmpData;
    }
}
