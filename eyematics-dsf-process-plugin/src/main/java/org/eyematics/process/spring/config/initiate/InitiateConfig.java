package org.eyematics.process.spring.config.initiate;

import org.eyematics.process.EyeMaticsProcessPluginDeploymentStateListener;
import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.message.initiate.GetDataMessageTask;
import org.eyematics.process.service.initiate.SelectTargetsTask;
import org.eyematics.process.spring.config.CryptoConfig;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;
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

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SelectTargetsTask selectTargetsTask() { return new SelectTargetsTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GetDataMessageTask getDataMessageTask() { return new GetDataMessageTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ProcessPluginDeploymentStateListener processPluginDeploymentStateListener()
    {
        return new EyeMaticsProcessPluginDeploymentStateListener(cryptoConfig.keyProviderDms());
    }
}
