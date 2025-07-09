/**
 * @author Hauke Hund (https://github.com/hhund)
 * @see    https://github.com/datasharingframework/dsf-process-ping-pong/blob/main/src/main/java/dev/dsf/bpe/listener/SetCorrelationKeyListener.java
 */

package org.eyematics.process.utils.listener;

import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.constants.BpmnExecutionVariables;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class CorrelationKeyProcessListener implements ExecutionListener, InitializingBean {

    private final ProcessPluginApi api;
    private static final Logger logger = LoggerFactory.getLogger(CorrelationKeyProcessListener.class);

    public CorrelationKeyProcessListener(ProcessPluginApi api)
    {
        this.api = api;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(api, "api");
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        logger.info("CorrelationKeyProvideProcessListener -> Adding Listener...");
        Variables variables = api.getVariables(execution);
        Target target = variables.getTarget();
        execution.setVariableLocal(BpmnExecutionVariables.CORRELATION_KEY, target.getCorrelationKey());
    }
}
