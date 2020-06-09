package com.avispl.symphony.dal.communicator.sample.aggregator;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGenerator;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.dal.communicator.HttpCommunicator;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Tag("test")
public class AggregatorCommunicatorTest {
    static AggregatorCommunicator aggregatorCommunicator;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().dynamicHttpsPort().bindAddress("127.0.0.1"));

    {
        wireMockRule.addMockServiceRequestListener(WireMockPactGenerator
                .builder("aggregator-adapter", "aggregator")
                .withRequestHeaderWhitelist("authorization", "content-type").build());
        wireMockRule.start();
    }

    @BeforeEach
    public void init() throws Exception {
        aggregatorCommunicator = new AggregatorCommunicator();
        aggregatorCommunicator.setTrustAllCertificates(true);
        aggregatorCommunicator.setProtocol("http");
        aggregatorCommunicator.setContentType("application/json");
        aggregatorCommunicator.setPort(wireMockRule.port());
        aggregatorCommunicator.setHost("127.0.0.1");
        aggregatorCommunicator.setAuthenticationScheme(HttpCommunicator.AuthenticationScheme.Basic);
        aggregatorCommunicator.setLogin("Admin");
        aggregatorCommunicator.setPassword("1234");
        aggregatorCommunicator.init();
    }

    @Test
    public void authenticationIsSuccessful() throws Exception {
        aggregatorCommunicator.authenticate();
        Assert.assertFalse(aggregatorCommunicator.getLoginId().isEmpty());
    }

    @Test
    public void getDevicesTest() throws Exception {
        List<AggregatedDevice> devices = aggregatorCommunicator.retrieveMultipleStatistics();
        Assert.assertFalse(devices.isEmpty());
        Assert.assertEquals("03275657", devices.get(0).getSerialNumber());
        Assert.assertEquals("Reboot", ((AdvancedControllableProperty.Button)devices.get(0).getControllableProperties().get(0).getType()).getLabel());
    }
}
