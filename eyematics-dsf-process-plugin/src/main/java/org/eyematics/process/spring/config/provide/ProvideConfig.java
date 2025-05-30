package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.provide.ProvideDataMessageTask;
import org.eyematics.process.service.provide.*;
import org.eyematics.process.spring.config.CryptoConfig;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
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
    private ProvideFhirClientConfig dicFhirClientConfig;

    @Autowired
    private DataSetStatusGenerator dataSetStatusGenerator;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReadProvideDataTask readProvideDataTask() { return new ReadProvideDataTask(api, dicFhirClientConfig.fhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CreateDataBundleTask createDataBundleTask() { return new CreateDataBundleTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EncryptDataBundleTask encryptDataBundleTask() { return new EncryptDataBundleTask(api, cryptoConfig.keyProviderDms()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StoreProvideDataTask storeProvideDataTask() { return new StoreProvideDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectReceiveTargetTask selectReceiveTargetTask() { return new SelectReceiveTargetTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ProvideDataMessageTask provideDataMessageTask() { return new ProvideDataMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleProvideErrorTask handleProvideErrorTask() { return  new HandleProvideErrorTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DeleteProvideDataTask deleteProvideDataTask() { return  new DeleteProvideDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StoreDataReceiptTask storeDataReceiptTask() { return  new StoreDataReceiptTask(api, dataSetStatusGenerator); }

}
