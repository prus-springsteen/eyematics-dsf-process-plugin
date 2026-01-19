package org.eyematics.process.spring.config.initiate;

import org.eyematics.process.message.initiate.CloseReceiveSubProcessInitiateMessageTask;
import org.eyematics.process.message.initiate.InitiateReceiveProcessMessageTask;
import org.eyematics.process.service.initiate.*;
import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.initiate.RequestDataMessageTask;
import org.eyematics.process.spring.config.receive.CryptoConfig;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class InitiateConfig {

    @Autowired
    private ProcessPluginApi api;

    @Autowired
    private CryptoConfig cryptoConfig;

    @Autowired
    private DataSetStatusGenerator dataSetStatusGenerator;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectReceiveTargetTask initiateReceiveTargetTask() { return new SelectReceiveTargetTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectRequestTargetsTask selectRequestTargetsTask() { return new SelectRequestTargetsTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InitiateReceiveProcessMessageTask initiateReceiveProcessTask() { return new InitiateReceiveProcessMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RequestDataMessageTask requestDataMessageTask() { return new RequestDataMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrepareCloseReceiveTarget prepareCloseReceiveTarget() { return new PrepareCloseReceiveTarget(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CloseReceiveSubProcessInitiateMessageTask closeReceiveProcessInitiateMessageTask() { return new CloseReceiveSubProcessInitiateMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HandleMissingInitiationTask handleMissingInitiationTask() { return new HandleMissingInitiationTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeInitiateSubProcessTask finalizeInitiateSubProcessTask() { return new FinalizeInitiateSubProcessTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeInitiateProcessTask finalizeInitiateProcessTask() { return new FinalizeInitiateProcessTask(api, dataSetStatusGenerator); }
}
