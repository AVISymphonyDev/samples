package com.avispl.symphony.dal.communicator.sample.controls;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGenerator;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Tag("test")
public class ControlsCommunicatorTest {
    static ControlsCommunicator controlsCommunicator;

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
        controlsCommunicator = new ControlsCommunicator();
    }

    @Test
    public void getControlsTest() throws Exception {
        List<Statistics> statistics = controlsCommunicator.getMultipleStatistics();
        ExtendedStatistics extendedStatistics = (ExtendedStatistics) statistics.get(0);

        Assert.assertFalse(extendedStatistics.getControllableProperties().isEmpty());
        Assert.assertFalse(extendedStatistics.getStatistics().isEmpty());
    }

    @Test
    public void triggerButtonActionTest() throws Exception {
        ControllableProperty buttonControl = new ControllableProperty();
        buttonControl.setProperty("Button");

        controlsCommunicator.controlProperty(buttonControl);
        Assert.assertEquals(controlsCommunicator.getControlsStatus().get("Button"), "Pressed");
    }

    @Test
    public void triggerDropDownActionTest() throws Exception {
        ControllableProperty dropDownControl = new ControllableProperty();
        dropDownControl.setProperty("DropDown");
        dropDownControl.setValue("Option value 1");

        controlsCommunicator.controlProperty(dropDownControl);
        Assert.assertEquals(controlsCommunicator.getControlsStatus().get("DropDown"), dropDownControl.getValue());
    }

    @Test
    public void triggerPresetActionTest() throws Exception {
        ControllableProperty presetControl = new ControllableProperty();
        presetControl.setProperty("Preset");
        presetControl.setValue("Option value 1");

        controlsCommunicator.controlProperty(presetControl);
        Assert.assertEquals(controlsCommunicator.getControlsStatus().get("Preset"), presetControl.getValue());
    }

    @Test
    public void triggerTriggerActionTest() throws Exception {
        ControllableProperty triggerControl = new ControllableProperty();
        triggerControl.setProperty("Switch");
        triggerControl.setValue(true);

        controlsCommunicator.controlProperty(triggerControl);
        Assert.assertEquals(Boolean.parseBoolean(String.valueOf(controlsCommunicator.getControlsStatus().get("Switch"))), triggerControl.getValue());
    }

    @Test
    public void triggerSliderActionTest() throws Exception {
        ControllableProperty sliderControl = new ControllableProperty();
        sliderControl.setProperty("Slider");
        sliderControl.setValue(1.5f);

        controlsCommunicator.controlProperty(sliderControl);
        Assert.assertEquals(Float.parseFloat(String.valueOf(controlsCommunicator.getControlsStatus().get("Slider"))), sliderControl.getValue());
    }

    @Test
    public void triggerNumericActionTest() throws Exception {
        ControllableProperty numericControl = new ControllableProperty();
        numericControl.setProperty("Numeric");
        numericControl.setValue(10);

        controlsCommunicator.controlProperty(numericControl);
        Assert.assertEquals(Integer.parseInt(String.valueOf(controlsCommunicator.getControlsStatus().get("Numeric"))), numericControl.getValue());
    }

    @Test
    public void triggerTextActionTest() throws Exception {
        ControllableProperty textControl = new ControllableProperty();
        textControl.setProperty("Text");
        textControl.setValue("TextValue");

        controlsCommunicator.controlProperty(textControl);
        Assert.assertEquals(controlsCommunicator.getControlsStatus().get("Text"), textControl.getValue());
    }
}
