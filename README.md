# opentracing-tracer
Spring Boot autoconfiguration for OpenTracing tracer implementations

## Usage

Clone the project and install it to your local Maven repository using 

```bash
    $ mvn clean install
```

In a Spring Boot project for which auto-configuration of a Jaeger implementation of `io.opentracing.Tracer` is needed,
include the starter by adding the following to `pom.xml`

```xml
<dependency>
    <groupId>me.snowdrop</groupId>
    <artifactId>opentracing-tracer-jaeger-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration options

All the available configuration options can be seen in `me.snowdrop.opentracing.tracer.JaegerConfigurationProperties`.
The prefix to be used for these properties is `opentracing.jaeger`.

## Defaults

If no configuration options are changed and the user does not manually provide any of the beans that the 
auto-configuration process provides, the following defaults are used:

* `spring-boot` Will be used as the service-name
* A `CompositeReporter` is provided which does not contain any delegates - effectively functioning as a Noop `Reporter`
* A `ConstSampler` with the value of `true`. This means that every trace will be sampled
* A `NullStatsReporter` is used - effectively meaning that no stats will be collected about the reporting of traces

## Common cases

### Set service name 

Set `opentracing.jaeger.serviceName` to the desired name

### Define an HTTP collector

Set `opentracing.jaeger.httpSenderProperties.url` to the URL of the Jaeger collector

### Define a UDP collector

Set `opentracing.jaeger.udpSenderProperties.host` to the host of the Jaeger collector
and `opentracing.jaeger.udpSenderProperties.port` to the end of the Jaeger collector

### Enable logging of spans

Set `opentracing.jaeger.httpSenderProperties.logSpans` to `true`

### Use a probabilistic sampler 

Set `opentracing.jaeger.probabilisticSampler.samplingRate` to a value between `0.0` (no sampling) and `1.0` (sampling of every request)

### Propagate headers in B3 format (for compatibility with Zipkin collectors)

Set `opentracing.jaeger.enableB3Propagation` to `true`

## Advanced cases

### com.uber.jaeger.Tracer.Builder customization

Before creating the `Tracer` it is possible to provide arbitrary customizations to `Tracer.Builder` by providing a bean
of type `JaegerTracerCustomizer`

### Add custom reporter while maintaining the ability to autoconfigure standard ones with properties

By supplying a bean of `ReporterAppender` the user can add custom as many custom `Reporter` as needed without
having the forgo the ability to configure the standard reportes via auto-configuration

### Use OpenTracing's TracerResolver

If the user want's to construct the Tracer using `TracerResolver.resolveTracer` the property `opentracing.jaeger.useTracerResolver`
needs to be set to true (in addition of course to `opentracing.jaeger.enable` being set to true)
In such a case none of this project's defaults are used and neither are the other configuration properties consulted.
Check out the [documentation](https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/README.md) of TracerResolver
for details on what properties need to be set.
Those properties mentioned in the documentation can be set using any method Spring Boot supports and are not limited
to System Properties and Environment Variables that TraceResolver supports out of the box