package org.eyematics.process.message.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import org.hl7.fhir.r4.model.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.eyematics.process.constant.EyeMaticsConstants.*;

public class SendProvideDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(SendProvideDataMessageTask.class);

    public SendProvideDataMessageTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");

        int min = 1000;
        int max = 5000;
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(min, max));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Stream.of(api.getTaskHelper()
                            .createInput(new Reference().setReference(variables.getString(BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE)),
                                                                                          CODE_SYSTEM_RECEIVE_PROCESS,
                                                                                          CODE_SYSTEM_RECEIVE_PROCESS_REFERENCE));
    }
}
