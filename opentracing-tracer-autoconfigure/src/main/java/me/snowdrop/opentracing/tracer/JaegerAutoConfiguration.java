/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.snowdrop.opentracing.tracer;

import com.uber.jaeger.Tracer.Builder;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.samplers.Sampler;
import com.uber.jaeger.tracerresolver.JaegerTracerResolver;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Configuration
@ConditionalOnClass(com.uber.jaeger.Tracer.class)
@ConditionalOnMissingBean(io.opentracing.Tracer.class)
@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JaegerConfigurationProperties.class)
public class JaegerAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.useTracerResolver", havingValue = "false", matchIfMissing = true)
    public static class ExplicitConfiguration {
        @Autowired(required = false)
        private List<JaegerTracerCustomizer> tracerCustomizers = Collections.emptyList();

        @Bean
        public io.opentracing.Tracer tracer(JaegerConfigurationProperties jaegerConfigurationProperties,
                                            Sampler sampler,
                                            Reporter reporter) {

            final Builder builder = new Builder(jaegerConfigurationProperties.getServiceName(), reporter, sampler);

            tracerCustomizers.forEach(c -> c.customize(builder));

            return builder.build();
        }

        @ConditionalOnMissingBean(Sampler.class)
        @Bean
        public Sampler sampler() {
            return new ConstSampler(true); //TODO Ponder this since its probably not the best default
        }

        /**
         * Configure a remote Reporter
         * TODO refactor to take into account the existence of CompositeReporter
         */
        @ConditionalOnMissingBean(Reporter.class)
        @Bean
        public Reporter remoteReporter(JaegerConfigurationProperties jaegerConfigurationProperties,
                                       Metrics reporterMetrics) {

            //TODO implement
            return null;
        }

        @ConditionalOnMissingBean(Metrics.class)
        @Bean
        public Metrics reporterMetrics() {
            return new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
        }
    }


    /**
     * TODO perhaps in this case we can map some of the properties from the Spring environment to System Properties
     * that TracerResolver expects
     */
    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.useTracerResolver", havingValue = "true")
    @ConditionalOnClass(JaegerTracerResolver.class)
    public static class TracerResolverConfiguration {

        @Bean
        public io.opentracing.Tracer tracer() {
            return TracerResolver.resolveTracer();
        }
    }
}
