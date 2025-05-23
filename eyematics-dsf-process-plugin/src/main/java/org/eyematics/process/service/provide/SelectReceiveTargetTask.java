/**
 * @author Mathias Rühle  (https://github.com/EmteZogaf)
 * @see    https://github.com/medizininformatik-initiative/mii-process-feasibility/blob/develop/mii-process-feasibility/src/main/java/de/medizininformatik_initiative/process/feasibility/service/SelectResponseTarget.java
 */

package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.constants.CodeSystems;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import static java.lang.String.format;

public class SelectReceiveTargetTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectReceiveTargetTask.class);

    public SelectReceiveTargetTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to select");
        Task task = variables.getStartTask();
        String correlationKey = api.getTaskHelper()
                .getFirstInputParameterStringValue(task, CodeSystems.BpmnMessage.URL, CodeSystems.BpmnMessage.Codes.CORRELATION_KEY).get();
        Identifier organizationIdentifier = task.getRequester().getIdentifier();
        Endpoint endpoint = api.getOrganizationProvider()
                .getOrganization(organizationIdentifier)
                .map(Organization::getEndpointFirstRep)
                .map(Reference::getReference)
                .map(r -> {
                    FhirWebserviceClient client = api.getFhirWebserviceClientProvider().getLocalWebserviceClient();
                    String path = URI.create(r).getPath();
                    return client.read(Endpoint.class, path.substring(path.lastIndexOf("/") + 1));
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        format("No endpoint found for organization with identifier '%s'",
                                organizationIdentifier.getValue())));
        Target target = variables.createTarget(organizationIdentifier.getValue(), endpoint.getIdentifierFirstRep().getValue(), endpoint.getAddress(), correlationKey);
        variables.setTarget(target);
        logger.info("-> {}", target);
    }
}
