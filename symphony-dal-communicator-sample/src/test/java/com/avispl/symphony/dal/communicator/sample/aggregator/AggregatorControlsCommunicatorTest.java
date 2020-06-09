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
public class AggregatorControlsCommunicatorTest {
    static AggregatorControlsCommunicator aggregatorCommunicator;

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
        aggregatorCommunicator = new AggregatorControlsCommunicator();
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
    public void getDeviceControlsTest() throws Exception {
        List<AggregatedDevice> devices = aggregatorCommunicator.retrieveMultipleStatistics();
        Assert.assertFalse(devices.isEmpty());
        Assert.assertEquals("03275657", devices.get(0).getSerialNumber());

        AdvancedControllableProperty dropDown = devices.get(0).getControllableProperties().get(0);
        Assert.assertEquals(4, ((AdvancedControllableProperty.DropDown)dropDown.getType()).getLabels().length);
        Assert.assertEquals(4, ((AdvancedControllableProperty.DropDown)dropDown.getType()).getOptions().length);
        Assert.assertEquals("Option1", dropDown.getValue());
        Assert.assertEquals("DropDown", dropDown.getName());

        AdvancedControllableProperty switchControl = devices.get(0).getControllableProperties().get(1);
        Assert.assertEquals("On", ((AdvancedControllableProperty.Switch)switchControl.getType()).getLabelOn());
        Assert.assertEquals("Off", ((AdvancedControllableProperty.Switch)switchControl.getType()).getLabelOff());
        Assert.assertEquals("true", switchControl.getValue());
        Assert.assertEquals("Switch", switchControl.getName());

        AdvancedControllableProperty slider = devices.get(0).getControllableProperties().get(2);
        Assert.assertEquals("End", ((AdvancedControllableProperty.Slider)slider.getType()).getLabelEnd());
        Assert.assertEquals("Start", ((AdvancedControllableProperty.Slider)slider.getType()).getLabelStart());
        Assert.assertEquals(1.0f, ((AdvancedControllableProperty.Slider)slider.getType()).getRangeStart().floatValue(), 0f);
        Assert.assertEquals(2.0f, ((AdvancedControllableProperty.Slider)slider.getType()).getRangeEnd().floatValue(), 0f);
        Assert.assertEquals("1.5", slider.getValue());
        Assert.assertEquals("Slider", slider.getName());

        AdvancedControllableProperty button = devices.get(0).getControllableProperties().get(3);
        Assert.assertEquals("Reboot", ((AdvancedControllableProperty.Button)button.getType()).getLabel());
        Assert.assertEquals("Rebooting", ((AdvancedControllableProperty.Button)button.getType()).getLabelPressed());
        Assert.assertEquals("", button.getValue());
        Assert.assertEquals("Button", button.getName());

        AdvancedControllableProperty preset = devices.get(0).getControllableProperties().get(4);
        Assert.assertEquals(4, ((AdvancedControllableProperty.Preset)preset.getType()).getLabels().length);
        Assert.assertEquals(4, ((AdvancedControllableProperty.Preset)preset.getType()).getOptions().length);
        Assert.assertEquals("Option1", preset.getValue());
        Assert.assertEquals("Preset", preset.getName());
    }
}
