# SAL sample adapter

Demo SAL adapter that schedules, retrieves and cancels a meeting

##Setup

### Build the module
`mvn clean install`

### Add maven dependency into the target project that is configured to use `symphony-sal` (i.e into `symphony-rest`)
```xml
<dependency>
    <groupId>com.avispl.symphony.sal</groupId>
    <artifactId>symphony-sal-sample</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Configure the application context
Add the following bean definition into the target application context customizing properties:
```xml
    <bean id="demoMeetingScheduler" class="com.avispl.symphony.sal.sample.DemoMeetingScheduler"
          init-method="scheduleAndCancelDemoMeeting">
        <property name="accountId" value="00000000-00000000-00000000-00000000"/> <!-- replace the value with actual account id -->
        <property name="userId" value="00000000-00000000-00000000-00000000"/> <!-- replace the value with actual user id -->
        <property name="meetingStart" value="1543667760000"/>
        <property name="meetingEnd" value="1543671360000"/>
        <property name="meetingOwner" value="somebody@avispl.com"/>  <!-- replace the value with actual email -->
        <property name="meetingRequester" value="somebody@avispl.com"/> <!-- replace the value with actual email -->
        <property name="participants">
            <util:set>
                <bean class="com.avispl.symphony.api.sal.dto.EmailParticipant">
                    <property name="email" value="somebody@avispl.com"/> <!-- replace the value with actual email -->
                    <property name="name" value="somebody"/>
                </bean>
            </util:set>
        </property>
    </bean>
```