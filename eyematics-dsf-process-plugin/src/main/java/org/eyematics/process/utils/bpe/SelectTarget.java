package org.eyematics.process.utils.bpe;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.constants.CodeSystems;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.*;
import java.net.URI;
import static java.lang.String.format;


public class SelectTarget {

    public static Target getRequestTarget(ProcessPluginApi api, Variables variables) {
        Task task = variables.getStartTask();
        String correlationKey = api.getTaskHelper().getFirstInputParameterStringValue(task, CodeSystems.BpmnMessage.URL, CodeSystems.BpmnMessage.Codes.CORRELATION_KEY).orElse(null);
        return SelectTarget.getRequestTarget(api, variables, correlationKey);
    }

    public static Target getRequestTarget(ProcessPluginApi api, Variables variables, String correlationKey) {
        Task task = variables.getStartTask();
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
        return variables.createTarget(organizationIdentifier.getValue(), endpoint.getIdentifierFirstRep().getValue(), endpoint.getAddress(), correlationKey);
    }

    public static Target getRequestTargetExecution(ProcessPluginApi api, DelegateExecution delegateExecution) {
        Variables variables = api.getVariables(delegateExecution);
        return variables.createTarget(variables.getTarget().getOrganizationIdentifierValue(),
                                      variables.getTarget().getEndpointIdentifierValue(),
                                      variables.getTarget().getEndpointUrl(),
                                      variables.getTarget().getCorrelationKey());
    }
}
