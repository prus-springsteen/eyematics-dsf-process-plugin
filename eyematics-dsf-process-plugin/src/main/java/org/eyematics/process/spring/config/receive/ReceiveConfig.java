package org.eyematics.process.spring.config.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.receive.AcknowledgeReceivedMessageTask;
import org.eyematics.process.service.receive.*;
import org.eyematics.process.spring.config.CryptoConfig;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ReceiveConfig {

    @Autowired
    private ProcessPluginApi api;

    @Autowired
    private CryptoConfig cryptoConfig;

    @Autowired
    private DataSetStatusGenerator dataSetStatusGenerator;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrepareReceiveDataTask prepareReceiveDataTask() { return new PrepareReceiveDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleReceiveMissingTask handleReceiveMissingTask() { return new HandleReceiveMissingTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DownloadRequestedDataTask downloadRequestedDataTask() { return new DownloadRequestedDataTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DecryptRequestedDataTask decryptRequestedDataTask() { return new DecryptRequestedDataTask(api, dataSetStatusGenerator, cryptoConfig.keyProviderDms()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleReceiveErrorTask handleReceiveErrorTask() { return new HandleReceiveErrorTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AcknowledgeReceivedMessageTask acknowledgeReceivedMessageTask() { return new AcknowledgeReceivedMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeReceiveProcessTask finalizeReceiveProcessTask() { return  new FinalizeReceiveProcessTask(api); }
}
