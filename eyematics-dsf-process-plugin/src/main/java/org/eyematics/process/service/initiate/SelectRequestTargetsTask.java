/**
 * @see    https://github.com/medizininformatik-initiative/mii-process-feasibility/blob/develop/mii-process-feasibility/src/main/java/de/medizininformatik_initiative/process/feasibility/service/SelectRequestTargets.java
 */
package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.common.auth.conf.Identity;
import dev.dsf.fhir.client.FhirWebserviceClient;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.List;
import java.util.UUID;


public class SelectRequestTargetsTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectRequestTargetsTask.class);

    public SelectRequestTargetsTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Selecting the Participating Organization(s)");
        Identifier parentIdentifier = new Identifier().setSystem(Identity.ORGANIZATION_IDENTIFIER_SYSTEM)
                                                      .setValue(EyeMaticsConstants.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_EYEMATICS);
        Coding memberOrganizationRole = new Coding().setSystem(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE).setCode(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC);
        FhirWebserviceClient client = api.getFhirWebserviceClientProvider().getLocalWebserviceClient();
        List<Target> targets = api.getOrganizationProvider().getOrganizations(parentIdentifier, memberOrganizationRole)
                .stream()
                .filter(Organization::hasEndpoint)
                .filter(Organization::hasIdentifier)
                .map(organization -> {
                    Identifier organizationIdentifier = organization.getIdentifierFirstRep();
                    String path = URI.create(organization.getEndpointFirstRep().getReference()).getPath();
                    Endpoint endpoint = client.read(Endpoint.class, path.substring(path.lastIndexOf("/") + 1));
                    return variables.createTarget(organizationIdentifier.getValue(),
                                                  endpoint.getIdentifierFirstRep().getValue(),
                                                  endpoint.getAddress(),
                                                  UUID.randomUUID().toString());
                }).toList();
        logger.info("-> Targets:\n{}", targets);
        variables.setTargets(variables.createTargets(targets));
    }
}
