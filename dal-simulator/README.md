## Description
This project is a DAL simulator. Main goal of this project is to provide 3rd party DAL adapter developers with capabilities of testing their adapters.

Running DAL simulator
---------------------

In order to run DAL simulator, following prerequisites needs to be met:
- JRE 1.8.x installed (`JRE_HOME/bin` must be on a PATH)

In order to run simulator, run following command
```
java -jar -DdevicesXmlPath=e:/dal-simulator-test/ symphony-dal-simulator.jar
```

Note to set `-DdevicesXmlPath` to a directory where `devices.xml` is located. 
During the startup DAL simulator will check for a devices.xml in the current folder which must exist in order to make simulator function properly.

After successful startup, Swagger becomes available by this URL: http://localhost:8080/swagger-ui.html

## How DAL simulator loads devices

`device.xml` comprises of following structure: it has a root element called `devices` that may contain 1..* of `device` elements.
`device` element describes a particular device and comprises of following items:
- `jar` - path to JAR file name DAL simulator needs to load
- `deviceClassName` - fully qualified class name to be instantiated
- `properties` - additional POJO properties that needs to be set after an instance of class specified in `deviceClassName` is created

Device instance creation lifecycle is:
- instance of `deviceClassName` is created by invoking a default constructor
- `properties` are set via POJO setters
- `init` method is called on `deviceClassName` instance

Example of devices.xml is shown below:

```xml
<devices>
    <device>
        <jar>e:/dal-simulator-test/symphony-dal-device-sample-1.0-SNAPSHOT.jar</jar>
        <deviceClassName>com.avispl.symphony.dal.device.sample.DeviceDalMock</deviceClassName>
        <properties>
            <property name="property1">property1 value</property>
        </properties>
    </device>
    <device>
        <jar>e:/dal-simulator-test/symphony-dal-device-sample-1.0-SNAPSHOT.jar</jar>
        <deviceClassName>com.avispl.symphony.dal.device.sample.DeviceDalMock2</deviceClassName>
        <properties/>
    </device>
    <device>
        <jar>e:/dal-simulator-test/symphony-dal-device-sample-1.0-SNAPSHOT</jar>
        <deviceClassName>com.avispl.symphony.dal.device.sample.DeviceDalAggregator</deviceClassName>
        <properties/>
    </device>
</devices>
```

It instructs DAL simulator that there are 3 devices it must load.

Sequence is following:
1. First device is presented by a `dal-device-mock-1.0-SNAPSHOT.jar`.
2. DAL simulator will check into deviceClassName field, read `com.test.DeviceDalMock` and instantiate this class.
3. DAL simulator will call `setProperty1("property1 value")` method on a device instance.
4. DAL simulator calls `init()` method on a device instance

Same then continues for `dal-device-mock2-1.0-SNAPSHOT.jar` and `dal-aggregator-mock1-1.0.jar`

When creating device instances, DAL simulator assigns random device IDs for them (in a form of UUID).

Log entries that confirms successful DAL simulator operation

```
2019-05-03 22:37:09.623  INFO 13344 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Found 3 deviceConfigurations in the \devices.xml
2019-05-03 22:37:09.625  INFO 13344 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Starting device initialization from configuration: com.avispl.symphony.dal.simulator.dto.DeviceConfiguration@4f18837a[jar=e:/dal-device-mock-1.0-SNAPSHOT.jar,deviceClassName=com.test.DeviceDalMock,properties=[com.avispl.symphony.dal.simulator.dto.DeviceProperty@4f6ee6e4[name=property1,value=property1 value], com.avispl.symphony.dal.simulator.dto.DeviceProperty@4466af20[name=property2,value=property2 value]]]
2019-05-03 22:37:09.682  INFO 13344 --- [           main] c.a.s.d.simulator.service.DeviceManager  : Device f96171b6-eb56-4c89-83ec-3fda76bca035 has following capabilities: com.avispl.symphony.dal.simulator.dto.RuntimeDeviceProperties@5158b42f[deviceClass=class com.test.DeviceDalMock,softwareVersion=1.2.3,address=127.0.0.1,isMonitorable=true,isPingable=true,isSnmpQuerable=false,isAggregator=false,isController=false]
2019-05-03 22:37:09.685  INFO 13344 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Device of class com.test.DeviceDalMock successfully initialized with instance com.test.DeviceDalMock@16293aa2
...
2019-05-03 22:37:10.686  INFO 13344 --- [pool-1-thread-1] c.a.s.d.simulator.service.DevicePoller   : Starting poll cycle for a device f96171b6-eb56-4c89-83ec-3fda76bca035
2019-05-03 22:37:10.689  INFO 13344 --- [pool-1-thread-1] c.a.s.d.simulator.service.DevicePoller   : Stats for device f96171b6-eb56-4c89-83ec-3fda76bca035 acquired:
- ping latency: 43 ms
- stats: com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics@67d6c589
```