# TAL sample adapter

A sample TAL adapter

##Setup

### Build the module
`mvn clean install`

### Add maven dependency into the target project that is configured to use `symphony-tal` (i.e into `symphony-rest`)
```xml
<dependency>
    <groupId>com.avispl.symphony.tal</groupId>
    <artifactId>symphony-tal-sample</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Configure the application context
Add the following bean definition into the target application context customizing properties:
```xml
<!-- TAL sample adapter -->
<bean id="talSampleAdapter" class="com.avispl.symphony.tal.sample.SampleTalAdapterImpl" init-method="init"/>
<bean id="talSampleTicketMapper" class="com.avispl.symphony.tal.sample.TicketMapper"/>
<bean id="talSampleInMemoryTicketSystem" class="com.avispl.symphony.tal.sample.InMemoryTicketSystem"/>
```

### Setup the ticket adapter configuration
Create a new adapter configuration of sync type `SampleAdapter` in Symphony's database.
This can be done adding a record into `core.tal_adapters` table of `vnoc_symphony` DB with an appropriate account identifier