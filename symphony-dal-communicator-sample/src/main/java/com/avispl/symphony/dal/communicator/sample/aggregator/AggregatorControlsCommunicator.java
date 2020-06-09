package com.avispl.symphony.dal.communicator.sample.aggregator;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.aggregator.parser.AggregatedDeviceProcessor;
import com.avispl.symphony.dal.aggregator.parser.PropertiesMapping;
import com.avispl.symphony.dal.aggregator.parser.PropertiesMappingParser;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class AggregatorControlsCommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {

    private String loginId;
    private ObjectMapper objectMapper;

    private AggregatedDeviceProcessor aggregatedDeviceProcessor;

    public static String BASE_URL = "/test/api/";

    public AggregatorControlsCommunicator() {
        super();
        setTrustAllCertificates(true);
        objectMapper = new ObjectMapper();
    }

    public String getLoginId() {
        return loginId;
    }

    @Override
    protected void internalInit() throws Exception {
        super.internalInit();
        Map<String, PropertiesMapping> mapping = new PropertiesMappingParser().loadYML("aggregator/model-mapping-controls.yml", getClass());
        aggregatedDeviceProcessor = new AggregatedDeviceProcessor(mapping);
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {
        String property = controllableProperty.getProperty();
        String deviceId = controllableProperty.getDeviceId();

        switch (property){
            case "Reboot":
                reboot(deviceId);
                break;
            default:
                logger.warn("Control operation " + property + " is not supported yet. Skipping.");
                break;
        }
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
    public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
        return fetchDevicesList();
    }

    @Override
    public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
        return retrieveMultipleStatistics()
                .stream()
                .filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Statistics> getMultipleStatistics() {
        ExtendedStatistics statistics = new ExtendedStatistics();
        List<AdvancedControllableProperty> controls = new ArrayList<>();
        Map<String, String> multipleStatistics = new HashMap<>();

        statistics.setStatistics(multipleStatistics);
        statistics.setControllableProperties(controls);
        return singletonList(statistics);
    }

    @Override
    protected void authenticate() throws Exception {
        JsonNode authentication = objectMapper.readTree(doPost(BASE_URL+"login",
                buildAuthenticationPayload(), String.class));
        loginId = authentication.get("LoginId").asText();
    }

    private Map<String, Map<String, String>> buildAuthenticationPayload(){
        Map<String, Map<String, String>> authenticationBody = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("userName", this.getLogin());
        credentials.put("password", this.getPassword());
        authenticationBody.put("credentials", credentials);
        return authenticationBody;
    }

    /**
     * aggregatedDeviceProcessor.extractDevices(JsonNode) generates a list of AggregatedDevice instances based on
     * the yml mapping defined like following
     *         Map<String, PropertiesMapping> mapping = new PropertiesMappingParser().loadYML("aggregator/model-mapping-controls.yml", getClass());
     * The AggregateDeviceProcessor is then initialized using this mapping
     *          aggregatedDeviceProcessor = new AggregatedDeviceProcessor(mapping);
     *
     * Basic mapping features:
     * Properties:
     *  Contains any custom data as well as placeholders for controls to be shown on UI
     * Mapping:
     *  Contains native aggregated device properties (serialNumber, deviceModel etc., check AggregatedDevice source code to see more)
     * Control:
     *  Is used to create controls for the device adapters. There are 2 ways to define a control - legacy and relevant one, legacy
     *  implies using AggregatedDevice.setControl(Map<String, String>), not supported by the aggregatedDeviceProcessor.
     *  Relevant version mapping implies control information definition like so:
     *         Button:                                              // Control name
     *           type: Button                                       // Control type
     *           label: Reboot                                      // Label
     *           labelPressed: Rebooting                            // Label after pressed
     *           gracePeriod: 0                                     // Device statistics pause period
     *
     *         Switch:                                              // Control name
     *           type: Switch                                       // Control type
     *           labelOn: "On"                                      // Label "on"
     *           labelOff: "Off"                                    // Label "off"
     *           value: true                                        // Current value
     *
     *         Preset:                                              // Control name
     *           type: Preset                                       // Control type
     *           labels: "Label1, Label2, Label3, Label4"           // List of options labels
     *           options: "Option1, Option2, Option3, Option4"      // List of options values
     *           value: "Option1"                                   // Current value
     *
     *         DropDown:                                            // Control name
     *           type: DropDown                                     // Control type
     *           labels: "Label1, Label2, Label3, Label4"           // List of options labels
     *           options: "Option1, Option2, Option3, Option4"      // List of options values
     *           value: "Option1"                                   // Current value
     *
     *         Slider:                                              // Control name
     *           type: Slider                                       // Control type
     *           rangeStart: 1.0                                    // Range start
     *           rangeEnd: 2.0                                      // Range end
     *           labelStart: "Start"                                // Starting label
     *           labelEnd: "End"                                    // Ending label
     *           value: 1.5                                         // Current value
     *
     *
     * */
    private List<AggregatedDevice> fetchDevicesList() throws Exception {
        List<AggregatedDevice> devices = aggregatedDeviceProcessor.extractDevices(getDevices());
        return devices;
    }

    private void reboot(String deviceSerialNumber) throws Exception {
        doPut(BASE_URL + "Devices/" + deviceSerialNumber + "/Reboot", String.class);
    }

    public JsonNode getDevices() throws Exception {
        authenticate();
        String devicesResponse = doGet(BASE_URL + "devices", String.class);
        JsonNode devices = objectMapper.readTree(devicesResponse);
        return devices;
    }

    @Override
    protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) {
        headers.set("Content-Type", "application/json");
        headers.set("SessionID", loginId);
        return headers;
    }

}
