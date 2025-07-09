/**
 * @see    https://github.com/medizininformatik-initiative/mii-process-data-sharing/blob/main/src/main/java/de/medizininformatik_initiative/process/data_sharing/message/SendMergeDataSharing.java
 */

package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.constants.NamingSystems;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.InitiateConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Queue;
import java.util.stream.Stream;

public class InitiateReceiveProcessesTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(InitiateReceiveProcessesTask.class);

    public InitiateReceiveProcessesTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        Targets initiateReceiveQueue = (Targets) execution.getVariable(InitiateConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_INITIATE_RECEIVE_QUEUE);
        if (initiateReceiveQueue.isEmpty()) {
            return Stream.empty();
        }
        Target target = initiateReceiveQueue.getEntries().get(0);
        initiateReceiveQueue = initiateReceiveQueue.removeByEndpointIdentifierValue(target);
        execution.setVariable(InitiateConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_INITIATE_RECEIVE_QUEUE, initiateReceiveQueue);
        return Stream.of(this.transformToTargetInput(target));
    }

    private Task.ParameterComponent transformToTargetInput(Target target) {
        Task.ParameterComponent input = api.getTaskHelper().createInput(new StringType(target.getCorrelationKey()),
                ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE,
                ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE_PROCESS_CORRELATION_KEY);

        input.addExtension().setUrl(ReceiveConstants.EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER)
                .setValue(new Reference().setIdentifier(NamingSystems.OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()))
                        .setType(ResourceType.Organization.name()));

        return input;
    }
}
