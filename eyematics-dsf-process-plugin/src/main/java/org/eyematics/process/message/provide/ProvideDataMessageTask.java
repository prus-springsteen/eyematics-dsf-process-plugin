package org.eyematics.process.message.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.hl7.fhir.r4.model.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public class ProvideDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(ProvideDataMessageTask.class);

    public ProvideDataMessageTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");

        /*
        // Concurrency-Exception: Sub-Tasks
        int min = 1000;
        int max = 5000;
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(min, max));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        */
        return Stream.of(api.getTaskHelper()
                            .createInput(new Reference().setReference(variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE)),
                                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS,
                                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_REFERENCE));
    }
}
