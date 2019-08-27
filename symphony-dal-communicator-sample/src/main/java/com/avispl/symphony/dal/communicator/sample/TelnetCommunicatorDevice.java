/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.sample;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.control.call.CallController;
import com.avispl.symphony.api.dal.control.mcu.MultipointControlUnit;
import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.VideoChannelStats;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.ping.Pingable;
import com.avispl.symphony.api.dal.snmp.SnmpQueryable;
import com.avispl.symphony.dal.communicator.TelnetCommunicator;

/**
 * An example of a device that utilizes DAL {@link TelnetCommunicator} library. <br>
 * This library provides convenient way to execute commands using telnet protocol in context of Symphony framework. It also implements {@link Pingable} and
 * {@link SnmpQueryable} interfaces used by Symphony to collect appropriate device statistics. <br>
 * <br>
 * {@link TelnetCommunicator} library has following configurable properties:
 * <ul>
 * <li>host - IP address/DNS name of device it will communicate with. This property is required</li>
 * <li>port - telnet port. This property is optional, default value: {@code 23}</li>
 * <li>login - user name to login with. This property is optional</li>
 * <li>password - password to login with. This property is optional</li>
 * <li>timeout - timeout for IO operations. This property is optional, default value: {@code 30000} ms</li>
 * <li>bufferSize - buffer size for IO operations. This property is optional, default value: {@code 4096} bytes</li>
 * </ul>
 * {@link TelnetCommunicator} also has set of properties allowing to configure parsing of response and error messages: <br>
 * <br>
 * <ul>
 * <li>loginPrompt - login prompt. Upon detecting this prompt, library with supply value of {@code login} property</li>
 * <li>passwordPrompt - password. Upon detecting this prompt, library with supply value of {@code password} property</li>
 * <li>loginSuccessList - one more strings which can be used as end of response indicator in case when login succeeded. Typically ends with command prompt</li>
 * <li>loginErrorList - one more strings which can be used as end of response indicator in case when login failed. Typically ends with login prompt</li>
 * <li>commandSuccessList - one more strings which can be used as end of response indicator in case when command succeeded. Typically ends with command
 * prompt</li>
 * <li>commandErrorList - one more strings which can be used as end of response indicator in case when command failed. Typically ends with command prompt</li>
 * </ul>
 * The response parsing properties, while fully configurable, typically remain the same for all devices of the same type/model supported by implementing class.
 * Therefore, it is a responsibility of implementing class to set them before library is initialized (e.g. in constructor). <br>
 * To determine end of response, library will first use corresponding error list, and then success list. If error response determined, library will throw
 * {@link CommandFailureException} containing failed command and its response. <br>
 * In case when it is not possible to reliable determine end of response messages using list of success and error message endings, implementing class should
 * override {@link TelnetCommunicator#doneReadingAfterConnect(String)} and/or {@link TelnetCommunicator#doneReading(String, String)} methods and provide custom
 * end of response parsing. If needed, custom end of response parsing can be done for specific commands only, and rest can be still delegated to parent library.
 * <br>
 * <br>
 * Note that while this sample only implements {@link Monitorable} interface, depending on device capabilities, there are other DAL interfaces which can be
 * implemented: {@link Controller}, {@link CallController}, {@link MultipointControlUnit}. <br>
 * <br>
 * Note also that this sample embeds local telnet server which is only used as a simulator for this sample and is not part of what this sample illustrates. <br>
 * <br>
 * 
 * @author Symphony Dev Team<br>
 *         Created on May 8, 2019
 */
public class TelnetCommunicatorDevice extends TelnetCommunicator implements Monitorable {

	// note: embedded telnet server is only used as a simulator for this sample and is not part of what this sample illustrates
	private Closeable telnet;
	//port of test server
    private int serverPort = 5334;

	/**
	 * TelnetCommunicatorDevice constructor.
	 */
	public TelnetCommunicatorDevice() {
		super();
		
		this.setHost("localhost");
		this.setPort(serverPort);
		// telnet server simulator used by this sample does not require login
		this.setLogin(null);
		this.setPassword(null);

		// set list of command success strings (included at the end of response when command succeeds, typically ending with command prompt)
		this.setCommandSuccessList(Collections.singletonList(""));
		// set list of error response strings (included at the end of response when command fails, typically ending with command prompt)
		this.setCommandErrorList(Collections.singletonList("ERROR"));
		// set list of login success strings (included at the end of response when login succeeds, typically ending with command prompt)
		this.setLoginSuccessList(Collections.singletonList(""));
		// set list of login error strings (included at the end of response when login fails, typically ending with login prompt)
		this.setLoginErrorList(Collections.singletonList("ERROR"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		// create telnet server listening on local port 5334
		// note: embedded telnet server is only used as a simulator for this sample and is not part of what this sample illustrates
		telnet = com.avispl.symphony.dal.communicator.sample.util.CommunicatorUtils.startTelnetServer(serverPort);

		// if needed, code to perform any extra initialization can be put here
		// also this method can be used to validate that device object is fully configured before use
		// e.g. if any properties are required by this class, code here can validate that they were set with valid values
		// note that TelnetCommunicator library already validates its own properties

		// otherwise, this method can be omitted

		// note also that exception thrown by this method will prevent object from being initialized and used
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean doneReadingAfterConnect(String response) throws FailedLoginException {
		// if it is not possible to reliable determine end of login response using list of success and error message endings, custom code to determine end of
		// login response can be placed here
		// otherwise, this method can be omitted

		return super.doneReadingAfterConnect(response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean doneReading(String command, String response) throws CommandFailureException {
		// if it is not possible to reliable determine end of command response using list of success and error message endings, custom code to determine end of
		// command response can be placed here. If needed, custom end of response parsing can be done for specific commands only, and rest can be still
		// delegated to parent library
		// otherwise, this method can be omitted

		return super.doneReading(command, response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		super.internalDestroy();

		// if needed, code to perform any extra clean up can be put here
		// otherwise, this method can be omitted

		// close telnet server simulator
		// note: embedded telnet server is only used as a simulator for this sample and is only used as a simulator for this sample and is not part of what this
		// sample illustrates
		try {
			if (telnet != null) {
				telnet.close();
			}
		} catch (IOException e) {
			// ignore
		}
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
		// send command via telnet and parse response to get statistics
		// for illustration purposes, this sample communicates with embedded telnet server simulator and uses response of one of commands simulator supports to
		// build monitoring statistics
		String response = send("hostname");

		// parse response data and extract available statistics
		// note that type of statistics depends on what monitoring information can be obtained from device
		int bitRateRx = response.length();

		// build appropriate statistics object
		// note that type of statistics object also depends on what monitoring information can be obtained from device
		// for generic computer statistics, use GenericStatistics
		// for call/audio/video statistics, use EndpointStatistics
		// for MCU, use MCUStatistics
		// for extended monitorable properties which are not defined in any known Statistics object, use ExtendedStatistics

		EndpointStatistics statistics = new EndpointStatistics();
		VideoChannelStats videoChannelStats = new VideoChannelStats();
		videoChannelStats.setBitRateRx(bitRateRx);
		statistics.setVideoChannelStats(videoChannelStats);

		// note that response might contain multiple statistics
		// e.g. ExtendedStatistics and ExtendedStatistics
		return Collections.singletonList(statistics);
	}

	/**
	 * Launches device sample. <br>
	 * Note this method is only here to demonstrate how sample works and does not need to be implemented in real device libraries.
	 *
	 * @param args no arguments is supported for now
	 * @throws Exception if any error occurs
	 */
	public static void main(String[] args) throws Exception {
		// Create and initialize device.
		TelnetCommunicatorDevice device = new TelnetCommunicatorDevice();
		device.setHost("localhost");
		device.setPort(5334);

		device.init();

		// Collect device statistics
		EndpointStatistics statistics = (EndpointStatistics) device.getMultipleStatistics().get(0);
		System.out.println("BitRateRx = " + statistics.getVideoChannelStats().getBitRateRx());

		device.destroy();
	}
}
