package org.eyematics.process.spring.config.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.receive.AcknowledgeReceivedMessageTask;
import org.eyematics.process.service.receive.*;
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
    private ReceiveFhirClientConfig receiveFhirClientConfig;

    @Autowired
    private DataSetStatusGenerator dataSetStatusGenerator;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrepareReceiveDataTask prepareReceiveDataTask() { return new PrepareReceiveDataTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public LogReceiveFailedTask logReceiveFailedTask() { return new LogReceiveFailedTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DownloadRequestedDataTask downloadRequestedDataTask() { return new DownloadRequestedDataTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DecryptRequestedDataTask decryptRequestedDataTask() { return new DecryptRequestedDataTask(api, dataSetStatusGenerator, cryptoConfig.keyProviderDicReceive()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InsertRequestedDataTask depositRequestedDataTask() { return new InsertRequestedDataTask(api, dataSetStatusGenerator, receiveFhirClientConfig.getFhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectProvideTargetTask selectProvideTargetTask() { return new SelectProvideTargetTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AcknowledgeReceivedMessageTask acknowledgeReceivedMessageTask() { return new AcknowledgeReceivedMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeReceiveSubprocessTask finalizeReceiveSubprocessTask() { return  new FinalizeReceiveSubprocessTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeReceiveProcessTask finalizeReceiveProcessTask() { return new FinalizeReceiveProcessTask(api, dataSetStatusGenerator); }
}
