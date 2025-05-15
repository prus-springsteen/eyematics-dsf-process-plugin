package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.provide.SendProvideDataMessageTask;
import org.eyematics.process.service.provide.*;
import org.eyematics.process.spring.config.CryptoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ProvideConfig {

    @Autowired
    private ProcessPluginApi api;

    @Autowired
    private CryptoConfig cryptoConfig;

    @Autowired
    private DicFhirClientConfig dicFhirClientConfig;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReadDataProvideTask readDataProvideTask() { return new ReadDataProvideTask(api, dicFhirClientConfig.fhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CreateProvideBundleDataTask createProvideBundleDataTask() { return new CreateProvideBundleDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EncryptProvideDataTask encryptProvideDataTask() { return new EncryptProvideDataTask(api, cryptoConfig.keyProviderDms()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectReceiveTargetTask selectReceiveTargetTask() { return new SelectReceiveTargetTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StoreProvideDataTask storeProvideDataTask() { return new StoreProvideDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SendProvideDataMessageTask sendProvideDataMessageTask() { return new SendProvideDataMessageTask(api); }
}
