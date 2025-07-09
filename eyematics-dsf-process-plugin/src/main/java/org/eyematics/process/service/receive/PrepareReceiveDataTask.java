package org.eyematics.process.service.receive;

import org.hl7.fhir.r4.model.Coding;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.constants.NamingSystems;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.hl7.fhir.r4.model.Reference;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ReceiveConstants;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PrepareReceiveDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PrepareReceiveDataTask.class);

    public PrepareReceiveDataTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to prepare");
        Task task = variables.getStartTask();
        List<Target> targetsList = this.getTargets(task, variables);
        Targets targets = variables.createTargets(targetsList);
        //variables.setTargets(targets);
        Target target = targets.getEntries().get(0);
        logger.info("-> Target: {}", target);
        variables.setTarget(target);
    }

    private List<Target> getTargets(Task task, Variables variables) {
        return api.getTaskHelper()
                .getInputParametersWithExtension(task, ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE,
                        ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE_PROCESS_CORRELATION_KEY, StringType.class,
                        ReceiveConstants.EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER)
                .map(p -> this.transformDicCorrelationKeyInputToTarget(p, variables)).toList();
    }

    private Target transformDicCorrelationKeyInputToTarget(Task.ParameterComponent input, Variables variables) {

        String organizationIdentifier = ((Reference) input
                .getExtensionByUrl(ReceiveConstants.EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER).getValue()).getIdentifier()
                .getValue();
        String correlationKey = ((StringType) input.getValue()).asStringValue();

        return api.getEndpointProvider().getEndpoint(NamingSystems.OrganizationIdentifier.withValue(
                                EyeMaticsConstants.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_EYEMATICS),
                        NamingSystems.OrganizationIdentifier.withValue(organizationIdentifier),
                        new Coding().setSystem(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE)
                                .setCode(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC))
                .map(e -> variables.createTarget(organizationIdentifier, e.getIdentifierFirstRep().getValue(),
                        e.getAddress(), correlationKey))
                .orElseThrow(() -> new RuntimeException(
                        "No endpoint of found for organization '" + organizationIdentifier + "'"));
    }
}
