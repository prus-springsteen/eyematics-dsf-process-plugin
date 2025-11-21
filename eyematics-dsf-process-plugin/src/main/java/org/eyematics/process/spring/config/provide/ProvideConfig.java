package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.interaction.EyeMaticsAdminApprovalTask;
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
    private ProvideAdminApprovalConfig provideAdminApprovalConfig;

    @Autowired
    private ProvideFhirClientConfig provideFhirClientConfig;

    @Autowired
    private ProvideFTTPClientConfig provideFTTPClientConfig;

    @Autowired
    private DataSetStatusGenerator dataSetStatusGenerator;

    @Autowired
    private ProvideMailConfig provideMailConfig;


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrepareProvideDataTask prepareProvideDataTask() { return new PrepareProvideDataTask(api, provideAdminApprovalConfig.isAdminApproval()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EyeMaticsAdminApprovalTask adminApprovalTask() { return new EyeMaticsAdminApprovalTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EvaluateAdminApprovalTask evaluateAdminApprovalTask() { return new EvaluateAdminApprovalTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReadProvideDataTask readProvideDataTask() { return new ReadProvideDataTask(api, dataSetStatusGenerator, provideFhirClientConfig.getFhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReadPatientDataTask readPatientDataTask() { return new ReadPatientDataTask(api, dataSetStatusGenerator, provideFhirClientConfig.getFhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CheckBroadConsentTask checkBroadConsentTask() { return new CheckBroadConsentTask(api, dataSetStatusGenerator, provideFhirClientConfig.getFhirClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ProcessGlobalPseudonymTask processGlobalPseudonymTask() { return new ProcessGlobalPseudonymTask(api, dataSetStatusGenerator, provideFTTPClientConfig.getFTTPClientFactory()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CreateDataBundleTask createDataBundleTask() { return new CreateDataBundleTask(api, dataSetStatusGenerator, provideMailConfig.getProvideInformationEmailAddresses()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EncryptDataBundleTask encryptDataBundleTask() { return new EncryptDataBundleTask(api, dataSetStatusGenerator, cryptoConfig.keyProviderDicReceive()); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StoreProvideDataTask storeProvideDataTask() { return new StoreProvideDataTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectReceiveTargetTask selectReceiveTargetTask() { return new SelectReceiveTargetTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ProvideDataMessageTask provideDataMessageTask() { return new ProvideDataMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleMissingReceiptTask handleMissingReceiptTask() { return  new HandleMissingReceiptTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DeleteProvideDataTask deleteProvideDataTask() { return  new DeleteProvideDataTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeProvideProcessTask storeDataReceiptTask() { return  new FinalizeProvideProcessTask(api, dataSetStatusGenerator); }

}
