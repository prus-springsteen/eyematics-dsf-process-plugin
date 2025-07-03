/**
 * @see    https://github.com/medizininformatik-initiative/mii-process-data-sharing/blob/main/src/main/java/de/medizininformatik_initiative/process/data_sharing/message/SendMergeDataSharing.java
 */

package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.v1.constants.NamingSystems;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ReceiveConstants;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Stream;

public class InitiateReceiveProcessTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(InitiateReceiveProcessTask.class);

    public InitiateReceiveProcessTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        Targets targets = variables.getTargets();
        List<Task.ParameterComponent> targetInputs = targets.getEntries().stream().map(this::transformToTargetInput).toList();
        logger.info("-> Correlation Keys and Targets -> {}", targetInputs);
        return targetInputs.stream();
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
