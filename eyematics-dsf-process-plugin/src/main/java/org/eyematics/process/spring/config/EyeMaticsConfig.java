package org.eyematics.process.spring.config;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.listener.CorrelationKeyProcessListener;
import org.eyematics.process.utils.listener.EyeMaticsProcessPluginDeploymentStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class EyeMaticsConfig {

    @Autowired
    private ProcessPluginApi api;

    @Autowired
    private CryptoConfig cryptoConfig;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ProcessPluginDeploymentStateListener processPluginDeploymentStateListener() {
        return new EyeMaticsProcessPluginDeploymentStateListener(cryptoConfig.keyProviderDicReceive());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CorrelationKeyProcessListener correlationKeyProvideProcessListener() {
        return new CorrelationKeyProcessListener(api);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DataSetStatusGenerator dataSetStatusGenerator() { return new DataSetStatusGenerator(); }
}
