/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.device.sample;

import com.avispl.symphony.api.dal.Device;
import com.avispl.symphony.api.dal.Version;
import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.api.dal.ping.Pingable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Simple minimalistic DAL aggregator sample with hardcoded/generated values
 * Real aggregators are supposed to be communicating with remote devices through network
 *
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class AggregatorSample implements Device, Pingable, Controller, Aggregator {

    /**
     * Fake devices managed by this aggregator
     */
    private List<AggregatedDevice> devices = new ArrayList<>();

    public String getAddress() {
        // IP address / hostname of the aggregator
        return "10.0.0.3";
    }

    public Version retrieveSoftwareVersion() throws Exception {
        // version of software / firmware target aggregator is running
        return new Version("1.12.3");
    }

    public void init() throws Exception {
        // this method is called after instance is created and
        // JavaBean properties are set
        System.out.printf("Initializing aggregator instance " + AggregatorSample.class);

        devices.addAll(Arrays.asList(
                ConfigAggregatedDevice.createDevice("Lights"),
                ConfigAggregatedDevice.createDevice("Projector"),
                ConfigAggregatedDevice.createDevice("Touchscreen")));
    }

    public boolean isInitialized() {
        // has to return true once aggregator is fully initialized
        return true;
    }

    public void destroy() {
        // this method is called upon a Symphony shutdown
        // here all persistent resources, sockets have to be released
        System.out.printf("Destroying aggregator instance " + AggregatorSample.class);
    }

    public int ping() throws Exception {
        // has to perform ping and return ping latency to a target device
        return new Random().nextInt(40);
    }

    public int getPingTimeout() {
        // ping timeout value
        return 60;
    }

    @Override
    public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {

        // Device aggregator has to return statistics for all devices it aggregates
        // see javadoc for com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice for all that needs to be returned
        // Code in this sample constructs dummy instances of AggregatedDevice with fake data
        // In real adapter following information needs to be retrieved from the target device and mapped to AggregatedDevice instance
        // using remote network calls

        return Collections.unmodifiableList(devices);
    }

    @Override
    public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
        // same as retrieveMultipleStatistics(), but just for given device identifiers
        return retrieveMultipleStatistics().stream()
                .filter(list::contains)
                .collect(Collectors.toList());
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) {

        // current implementation shows concept of "controlling a device"
        // here, some devices expose properties that might be controlled via AggregatedDevice.getControl()
        // those are properties through which device operation might be changed.
        // Here we just change a value in a Map, however, in real adapter, that should result in a network call
        // to propagate control request to a target device

        // no device ID supplied
        if (controllableProperty == null || controllableProperty.getDeviceId() == null)
            return;

        // looking for a device ID supplied in controllableProperty
        AggregatedDevice device = devices.stream()
                .filter(d -> d.getDeviceId().equals(controllableProperty.getDeviceId()))
                .findFirst()
                .orElse(null);

        // no such device found
        if (device == null)
            return;

        // device doesn't have controllable parameters
        if (device.getControl() == null)
            return;

        // device doesn't have controllable parameter specified from the outside
        if (device.getControl().get(controllableProperty.getProperty()) == null)
            return;

        // value should not be null
        if (controllableProperty.getValue() == null)
            return;

        device.getProperties().put(controllableProperty.getProperty(), controllableProperty.getValue().toString());
    }

    @Override
    public void controlProperties(List<ControllableProperty> controllableProperties) {
        // same as controlProperty(ControllableProperty controllableProperty), but for a multiples of ControllableProperty
        controllableProperties.stream().forEach(p -> controlProperty(p));
    }
}
