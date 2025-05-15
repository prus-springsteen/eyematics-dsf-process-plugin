package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class GetDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(GetDataMessageTask.class);

    public GetDataMessageTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> nothing to send");
        return Stream.empty();
    }
}
