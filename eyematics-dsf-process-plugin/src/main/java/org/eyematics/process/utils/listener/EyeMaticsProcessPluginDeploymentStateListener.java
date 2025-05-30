package org.eyematics.process.utils.listener;

import org.eyematics.process.utils.crypto.KeyProvider;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Objects;

import static org.eyematics.process.constant.InitiateConstants.PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS;

public class EyeMaticsProcessPluginDeploymentStateListener implements ProcessPluginDeploymentStateListener, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(EyeMaticsProcessPluginDeploymentStateListener.class);
    private final KeyProvider keyProvider;

    public EyeMaticsProcessPluginDeploymentStateListener(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    // https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2
    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(keyProvider, "keyProvider");
        // Ist im Docker irgendwo fest hinterlegt...
        Configurator.setLevel("org.eyematics", Level.INFO);
    }

    @Override
    public void onProcessesDeployed(List<String> activeProcesses) {
        logger.info("Providing Public Key, If Not Exists.");
        if (activeProcesses.contains(PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS)) {
            keyProvider.createPublicKeyIfNotExists();
        }
    }
}
