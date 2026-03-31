package org.eyematics.process.spring.config.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.receive.AcknowledgeReceivedMessageTask;
import org.eyematics.process.message.receive.InitiateProvideDataMessageTask;
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
    public PrepareReceiveDataTask prepareReceiveDataTask() {
        return new PrepareReceiveDataTask(api);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectInitiateTargetTask selectInitiateTargetTask() {
        return new SelectInitiateTargetTask(api);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InitiateProvideDataMessageTask initiateProvideDataMessageTask() {
        return new InitiateProvideDataMessageTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleMissingDataTask handleMissingDataTask() {
        return new HandleMissingDataTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CloseReceiveSubProcessTask closeReceiveSubprocessTask() {
        return new CloseReceiveSubProcessTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InformAdminTask informAdminTask() {
        return new InformAdminTask(api);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DownloadRequestedDataTask downloadRequestedDataTask() {
        return new DownloadRequestedDataTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DecryptRequestedDataTask decryptRequestedDataTask() {
        return new DecryptRequestedDataTask(api, dataSetStatusGenerator, cryptoConfig.keyProviderDicReceive());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InsertRequestedDataTask insertRequestedDataTask() {
        return new InsertRequestedDataTask(api,
                dataSetStatusGenerator,
                receiveFhirClientConfig.getFhirClientFactory());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectProvideTargetTask selectProvideTargetTask() {
        return new SelectProvideTargetTask(api);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AcknowledgeReceivedMessageTask acknowledgeReceivedMessageTask() {
        return new AcknowledgeReceivedMessageTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeReceiveSubProcessTask finalizeReceiveSubprocessTask() {
        return  new FinalizeReceiveSubProcessTask(api, dataSetStatusGenerator);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeReceiveProcessTask finalizeReceiveProcessTask() {
        return new FinalizeReceiveProcessTask(api, dataSetStatusGenerator);
    }
}
