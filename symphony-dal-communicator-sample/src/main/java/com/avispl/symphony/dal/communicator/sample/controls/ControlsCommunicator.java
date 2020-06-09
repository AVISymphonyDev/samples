package com.avispl.symphony.dal.communicator.sample.controls;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.Collections.singletonList;

public class ControlsCommunicator extends RestCommunicator implements Monitorable, Controller {
    private Map<String, String> controlsStatus = new HashMap<>();

    public ControlsCommunicator() {
        super();
        setTrustAllCertificates(true);
    }

    @Override
    protected void authenticate() throws Exception {

    }

    @Override
    protected void internalInit() throws Exception {
        super.internalInit();
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {
        String property = controllableProperty.getProperty();
        Object value = controllableProperty.getValue();
        
        switch (property){
            case "Button":
                buttonAction();     // We don't need to pass a button value anywhere because it's quite binary.
                break;
            case "Switch":
                switchAction(value);
                break;
            case "DropDown":
                dropdownAction(value);
                break;
            case "Preset":
                presetAction(value);
                break;
            case "Slider":
                sliderAction(value);
                break;
            case "Text":
                textAction(value);
                break;
            case "Numeric":
                numericAction(value);
                break;
            default:
                logger.warn("Control operation " + property + " is not supported yet. Skipping.");
                break;
        }
    }

    private void buttonAction() {
        System.out.println("Button press action");
        controlsStatus.put("Button", "Pressed");
    }

    private void dropdownAction(Object value) {
        System.out.println("Dropdown select action with value " + value);
        controlsStatus.put("DropDown", String.valueOf(value));
    }

    private void switchAction(Object value) {
        System.out.println("Switch action trigger with value " + value);
        controlsStatus.put("Switch", String.valueOf(value));
    }

    private void numericAction(Object value) {
        System.out.println("Numeric action trigger with value " + value);
        controlsStatus.put("Numeric", String.valueOf(value));
    }

    private void textAction(Object value) {
        System.out.println("Text action trigger with value " + value);
        controlsStatus.put("Text", String.valueOf(value));
    }

    private void sliderAction(Object value) {
        System.out.println("Slider action trigger with value " + value);
        controlsStatus.put("Slider", String.valueOf(value));
    }

    private void presetAction(Object value) {
        System.out.println("Preset action trigger with value " + value);
        controlsStatus.put("Preset", String.valueOf(value));
    }

    @Override
    public void controlProperties(List<ControllableProperty> controllablePropertyList) throws Exception {
        if (CollectionUtils.isEmpty(controllablePropertyList)) {
            throw new IllegalArgumentException("Controllable properties cannot be null or empty");
        }
        for(ControllableProperty controllableProperty: controllablePropertyList){
            controlProperty(controllableProperty);
        }
    }

    @Override
    public List<Statistics> getMultipleStatistics() {
        ExtendedStatistics statistics = new ExtendedStatistics();
        List<AdvancedControllableProperty> controls = new ArrayList<>();
        Map<String, String> multipleStatistics = new HashMap<>();

        AdvancedControllableProperty.Button buttonControl = new AdvancedControllableProperty.Button();
        buttonControl.setLabel("Button");                       // Default button label
        buttonControl.setLabelPressed("Pressed");               // Button name after pressed, indicates that the button press took action, gets back to default state after short period of time
        buttonControl.setGracePeriod(120000L);                  // Period to pause monitoring of the device for
        AdvancedControllableProperty buttonWrapper = new AdvancedControllableProperty("Button", new Date(), buttonControl, "");
        controls.add(buttonWrapper);
        multipleStatistics.put("Button", "");                   // In order to display the control on UI - name and value should match ^

        AdvancedControllableProperty.Switch switchControl = new AdvancedControllableProperty.Switch();
        switchControl.setLabelOn("On"); // Label indicating "On" state of the switch
        switchControl.setLabelOff("Off"); // Label indicating "Off" state of the switch
        AdvancedControllableProperty switchWrapper = new AdvancedControllableProperty("Switch", new Date(), switchControl, true);
        controls.add(switchWrapper);
        multipleStatistics.put("Switch", "true");               // In order to display the control on UI - name and value should match ^

        AdvancedControllableProperty.DropDown dropDownControl = new AdvancedControllableProperty.DropDown();
        dropDownControl.setLabels(new String[] {"Option 1", "Option 2", "Option 3"}); // Dropdown options labels
        dropDownControl.setOptions(new String[] {"Option 1 value", "Option 2 value", "Option 3 value"}); // Dropdown options values
        AdvancedControllableProperty dropdownWrapper = new AdvancedControllableProperty("DropDown", new Date(), dropDownControl, "Option 1 value");
        controls.add(dropdownWrapper);
        multipleStatistics.put("DropDown", "Option 1 value");   // In order to display the control on UI - name and value should match ^

        AdvancedControllableProperty.Preset presetControl = new AdvancedControllableProperty.Preset();
        presetControl.setLabels(new String[] {"Option 1", "Option 2", "Option 3"}); // Preset options labels
        presetControl.setOptions(new String[] {"Option 1 value", "Option 2 value", "Option 3 value"}); // Preset options values
        AdvancedControllableProperty presetWrapper = new AdvancedControllableProperty("Preset", new Date(), presetControl, "Option 1 value");
        controls.add(presetWrapper);
        multipleStatistics.put("Preset", "Option 1 value");     // In order to display the control on UI - name and value should match ^

        AdvancedControllableProperty.Slider sliderControl = new AdvancedControllableProperty.Slider();
        sliderControl.setRangeStart(1.0f);                      // Slider range start value
        sliderControl.setRangeEnd(2.0f);                        // Slider range end value
                                                                // Values are interpolated by 0.1
        sliderControl.setLabelStart("Min");                     // Label indicating range start
        sliderControl.setLabelEnd("Max");                       // Label indicating range end
        AdvancedControllableProperty sliderWrapper = new AdvancedControllableProperty("Slider", new Date(), sliderControl, 1.5f);
        controls.add(sliderWrapper);
        multipleStatistics.put("Slider", "1.5");                // In order to display the control on UI - name and value should match ^

        AdvancedControllableProperty.Text textControl = new AdvancedControllableProperty.Text();
        AdvancedControllableProperty textWrapper = new AdvancedControllableProperty("Text", new Date(), textControl, "TextValue");
        controls.add(textWrapper);
        multipleStatistics.put("Text", "TextValue");

        AdvancedControllableProperty.Numeric numericControl = new AdvancedControllableProperty.Numeric();
        AdvancedControllableProperty numericWrapper = new AdvancedControllableProperty("Numeric", new Date(), numericControl, 10);
        controls.add(numericWrapper);
        multipleStatistics.put("Numeric", "10");

        statistics.setStatistics(multipleStatistics);
        statistics.setControllableProperties(controls);

        return singletonList(statistics);
    }

    public Map getControlsStatus(){
        return controlsStatus;
    }
}
