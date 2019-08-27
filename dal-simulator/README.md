## Description
This project is a DAL simulator. Main goal of this project is to provide 3rd party DAL adapter developers with capabilities of testing their adapters.

Running DAL simulator
---------------------
In order to run DAL simulator, following prerequisites needs to be met:
- JRE 1.8.x installed (`JRE_HOME/bin` must be on a PATH)

In order to run simulator, run following command:
```
java -jar -DdevicesXmlPath=e:/dal-simulator-test/ symphony-dal-simulator.jar
```

Note to set `-DdevicesXmlPath` to a directory where `devices.xml` is located. 
During the startup DAL simulator will check for a devices.xml in the current folder which must exist in order to make simulator function properly.

If your device class uses third party libraries, please add needed jars in `lib` folder end use following command:
```
java -cp symphony-dal-simulator.jar -Dloader.path="./lib/*" -DdevicesXmlPath=e:/dal-simulator-test/ org.springframework.boot.loader.PropertiesLauncher
```

In our examples `SshCommunicatorDevice` uses sshd third party library, so it is necessarily to add `sshd-common` and `sshd-core` jars in lib folder.

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
        <jar>d:/dal-simulator-test/symphony-dal-communicator-sample-1.0-SNAPSHOT.jar</jar>
        <deviceClassName>com.avispl.symphony.dal.communicator.sample.SampleBaseDevice</deviceClassName>
        <properties>
            <property name="host">127.0.0.1</property>
        </properties>
    </device>
</devices>
```

It instructs DAL simulator that there is device it must load.

Sequence is following:
1. The device is presented by a `symphony-dal-communicator-sample-1.0-SNAPSHOT.jar`.
2. DAL simulator will check into deviceClassName field, read `com.avispl.symphony.dal.communicator.sample.SampleBaseDevice` and instantiate this class.
3. DAL simulator will call `setHost("127.0.0.1")` method on a device instance.
4. DAL simulator calls `init()` method on a device instance

When creating device instances, DAL simulator assigns random device IDs for them (in a form of UUID).

Log entries that confirms successful DAL simulator operation

```
2019-08-20 12:01:11.855  INFO 7524 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Found 1 deviceConfigurations in the d:\dal-simulator-test\dal-simulator\devices.xml
2019-08-20 12:01:11.856  INFO 7524 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Starting device initialization from configuration: com.avispl.symphony.dal.simulator.dto.DeviceConfiguration@795cd85e[jar=D:\dal-simulator-test\symphony-dal-communicator-sample\target\symphony-dal-communicator-sample-1.0-SNAPSHOT.jar,deviceClassName=com.avispl.symphony.dal.communicator.sample.SampleBaseDevice,properties=[com.avispl.symphony.dal.simulator.dto.DeviceProperty@123ef382[name=host,value=127.0.0.1]]]
2019-08-20 12:01:11.907  INFO 7524 --- [           main] c.a.s.d.s.service.LocalJarLoaderService  : Storing user JAR D:\dal-simulator-test\symphony-dal-communicator-sample\target\symphony-dal-communicator-sample-1.0-SNAPSHOT.jar in a temporary location C:\Users\User\AppData\Local\Temp\symphony-dal-communicator-sample-1.0-SNAPSHOT.jar7387561221302323235.jar
2019-08-20 12:01:11.908  INFO 7524 --- [           main] c.a.s.d.s.service.LocalJarLoaderService  : JAR C:\Users\User\AppData\Local\Temp\symphony-dal-communicator-sample-1.0-SNAPSHOT.jar7387561221302323235.jar has been successfully loaded
2019-08-20 12:01:11.991  INFO 7524 --- [           main] c.a.s.d.simulator.service.DeviceManager  : Successfully registered device instance com.avispl.symphony.dal.communicator.sample.SampleBaseDevice@5ffead27 with ID 54df8527-e6d2-4da6-a50d-d146aefb7b80
2019-08-20 12:01:11.992  INFO 7524 --- [           main] c.a.s.d.simulator.service.DeviceManager  : Device 54df8527-e6d2-4da6-a50d-d146aefb7b80 has following capabilities: com.avispl.symphony.dal.simulator.dto.RuntimeDeviceProperties@6356695f[deviceClass=class com.avispl.symphony.dal.communicator.sample.SampleBaseDevice,softwareVersion=<null>,address=127.0.0.1,isMonitorable=true,isPingable=true,isSnmpQuerable=true,isAggregator=false,isController=false]
2019-08-20 12:01:11.994  INFO 7524 --- [           main] c.a.s.d.s.s.DeviceBootstrapService       : Device of class com.avispl.symphony.dal.communicator.sample.SampleBaseDevice successfully initialized with instance com.avispl.symphony.dal.communicator.sample.SampleBaseDevice@5ffead27
...
2019-08-20 12:01:12.996  INFO 7524 --- [pool-2-thread-1] c.a.s.d.simulator.service.DevicePoller   : Starting poll cycle for a device 54df8527-e6d2-4da6-a50d-d146aefb7b80
2019-08-20 12:01:13.127  INFO 7524 --- [pool-2-thread-1] com.avispl.symphony.dal.util.IcmpUtils   : Using com.avispl.symphony.dal.util.IcmpUtils$SimpleWindowsNativeBridge for ICMP ping
2019-08-20 12:01:13.161  INFO 7524 --- [pool-2-thread-1] c.a.s.d.simulator.service.DevicePoller   : Stats for device 54df8527-e6d2-4da6-a50d-d146aefb7b80 acquired:
- ping latency: 1 ms
- stats: [com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics@d35ddf2]
```
