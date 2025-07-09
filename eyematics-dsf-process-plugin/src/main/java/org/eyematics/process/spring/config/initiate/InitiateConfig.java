package org.eyematics.process.spring.config.initiate;

import org.eyematics.process.message.initiate.InitiateReceiveProcessesTask;
import org.eyematics.process.service.initiate.SetRequestTargetsTask;
import org.eyematics.process.service.initiate.FinalizeInitiateProcessTask;
import org.eyematics.process.service.initiate.InitiateReceiveTargetsTask;
import org.eyematics.process.service.initiate.SelectRequestTargetsTask;
import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.initiate.RequestDataMessageTask;
import org.eyematics.process.spring.config.CryptoConfig;
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
    public SelectRequestTargetsTask selectRequestTargetsTask() { return new SelectRequestTargetsTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InitiateReceiveTargetsTask initiateReceiveTargetsTask() { return new InitiateReceiveTargetsTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InitiateReceiveProcessesTask initiateReceiveProcessesTask() { return new InitiateReceiveProcessesTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SetRequestTargetsTask setRequestTargetsTask() {return new SetRequestTargetsTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RequestDataMessageTask requestDataMessageTask() { return new RequestDataMessageTask(api, dataSetStatusGenerator); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FinalizeInitiateProcessTask finalizeInitiateProcessTask() { return new FinalizeInitiateProcessTask(api, dataSetStatusGenerator); }
}
