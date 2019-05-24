/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.device.sample;

import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates instances of AggregatedDevice for testing purposes
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class ConfigAggregatedDevice {
    private static final String CONTROLLER_SERIAL_NUMBER = "mock_amx_web_server";

    private static final List<String> DEVICE_TYPES = Arrays.asList(
            "Camera", "RoomControls", "Controller", "Display", "DSP", "Touchscreen",
            "Amplifier", "OccupancySensor", "InputSource", "Switcher", "VideoCodec",
            "Projector", "Shades", "Lights", "Generic", "Unknown");

    List<AggregatedDevice> controllerDevices = new ArrayList<>();

    public static AggregatedDevice createController(boolean controllerDevice) {
        AggregatedDevice aggregatedDevice = createDevice("Controller");
        aggregatedDevice.setSerialNumber(CONTROLLER_SERIAL_NUMBER);
        aggregatedDevice.setStatistics(updateStatistics(null, aggregatedDevice.getDeviceType()));
        aggregatedDevice.setProperties(updateProperties(null, aggregatedDevice.getDeviceType()));
        return aggregatedDevice;
    }

    public static AggregatedDevice createDevice(String deviceType) {
        AggregatedDevice aggregatedDevice = new AggregatedDevice();

        aggregatedDevice.setAviSplAssetId(Randoms.randomString());
        aggregatedDevice.setDeviceMake(Randoms.randomString());
        aggregatedDevice.setDeviceModel(Randoms.randomString());
        aggregatedDevice.setDeviceName(Randoms.randomString());
        aggregatedDevice.setDeviceOnline(Boolean.TRUE);
        aggregatedDevice.setOwnerAssetId(Randoms.randomString());

        aggregatedDevice.setSerialNumber(Randoms.randomString());
        aggregatedDevice.setDeviceType(deviceType);

        aggregatedDevice.setDeviceId(UUID.randomUUID().toString());
        aggregatedDevice.setStatistics(updateStatistics(null, aggregatedDevice.getDeviceType()));
        aggregatedDevice.setProperties(updateProperties(null, aggregatedDevice.getDeviceType()));
        aggregatedDevice.setControl(updateControl(null, aggregatedDevice.getDeviceType()));

        return aggregatedDevice;
    }

    public static Map<String, String> updateStatistics(Map<String, String> existingStatistics, String deviceType) {
        Map<String, String> returnStatistics;
        if (null != existingStatistics) {
            returnStatistics = existingStatistics;
        } else {
            returnStatistics = new HashMap<>();
        }

        switch (deviceType) {
            case "Controller":
                returnStatistics.putIfAbsent("firmwareVersion", "1.1001.12");
                returnStatistics.putIfAbsent("firmwareDate", (new Date(Randoms.randomLong())).toString());
                returnStatistics.putIfAbsent("macAddress", Randoms.randomMacAddress());
                returnStatistics.putIfAbsent("ipAddress", Randoms.randomIPAddress());
                returnStatistics.putIfAbsent("hostname", Randoms.randomString() + ".local");
                returnStatistics.putIfAbsent("serialNumber", Randoms.randomString());
                returnStatistics.putIfAbsent("systemName", Randoms.randomString());
                returnStatistics.putIfAbsent("fileName", Randoms.randomString() + ".dat");
                returnStatistics.putIfAbsent("compiledOn", (new Date(Randoms.randomLong())).toString());
                returnStatistics.putIfAbsent("compilePath", "/var/db/" + Randoms.randomString());
                break;
            case "Display":
                break;
            case "DSP":
                break;
            case "Touchscreen":
                returnStatistics.putIfAbsent("macAddress", Randoms.randomString());
                returnStatistics.putIfAbsent("ipAddress", Randoms.randomIPAddress());
                returnStatistics.putIfAbsent("projectName", Randoms.randomString());
                break;
            case "Amplifier":
                returnStatistics.putIfAbsent("faltCondition", Randoms.randomString());
                break;
            case "OccupancySensor":
                break;
            case "InputSource":
                break;
            case "Switcher":
                returnStatistics.putIfAbsent("whatsRouted", Randoms.randomString());
                returnStatistics.putIfAbsent("inputName", Randoms.randomString());
                break;
            case "Projector":
                returnStatistics.putIfAbsent("selectedInput", Randoms.randomString());
                returnStatistics.putIfAbsent("lampHours", String.valueOf(Randoms.randomInt()));
                break;
            case "VideoCodec":
                returnStatistics.putIfAbsent("ipAddress", Randoms.randomIPAddress());
                break;
            default:
                break;
        }

        return returnStatistics;
    }

    public static Map<String, String> updateControl(Map<String, String> existingProperties, String deviceType) {
        Map<String, String> returnProperties;
        if (null != existingProperties) {
            returnProperties = existingProperties;
        } else {
            returnProperties = new HashMap<>();
        }

        switch (deviceType) {
            case "Lights":
                returnProperties.put("lightsOn", "Toggle");
                break;
            default:
                break;
        }

        return returnProperties;
    }

    public static Map<String, String> updateProperties(Map<String, String> existingProperties, String deviceType) {
        Map<String, String> returnProperties;
        if (null != existingProperties) {
            returnProperties = existingProperties;
        } else {
            returnProperties = new HashMap<>();
        }

        switch (deviceType) {
            case "Display":
                returnProperties.put("powerOn", String.valueOf(Randoms.randomBoolean()));
                returnProperties.put("input", Randoms.randomString());
                break;
            case "Lights":
                returnProperties.put("lightsOn", "0");
                break;
            default:
                break;
        }

        return returnProperties;
    }
}
