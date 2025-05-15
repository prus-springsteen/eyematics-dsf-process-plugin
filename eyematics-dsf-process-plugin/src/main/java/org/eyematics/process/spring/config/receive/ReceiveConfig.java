package org.eyematics.process.spring.config.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.service.receive.DecryptDataReceiveTask;
import org.eyematics.process.service.receive.DownloadDataReceiveTask;
import org.eyematics.process.spring.config.CryptoConfig;
import org.eyematics.process.utils.listener.SetCorrelationKeyListener;
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

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SetCorrelationKeyListener setCorrelationKeyListener() { return new SetCorrelationKeyListener(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DownloadDataReceiveTask downloadDataReceiveTask() { return new DownloadDataReceiveTask(api); }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DecryptDataReceiveTask decryptDataReceiveTask() { return new DecryptDataReceiveTask(api, cryptoConfig.keyProviderDms()); }
}
