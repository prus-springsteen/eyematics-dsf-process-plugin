package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.eyematics.process.constant.EyeMaticsConstants.*;
import static dev.dsf.common.auth.conf.Identity.ORGANIZATION_IDENTIFIER_SYSTEM;

public class SelectTargetsTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectTargetsTask.class);

    public SelectTargetsTask(ProcessPluginApi api) {
        super(api);
        System.out.println("Logger-Name: " + logger.getName());
        System.out.println("Logger-Level: " + logger.atInfo());
        System.out.println("Logger-Debug: " + logger.isDebugEnabled());
        System.out.println("Logger-Info: " + logger.isInfoEnabled());
        System.out.println("Logger-Error: " + logger.isErrorEnabled());
        System.out.println("Logger-Trace: " + logger.isTraceEnabled());
        System.out.println("Logger-Warn: " + logger.isWarnEnabled());
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Triggering the Organization(s)");
        Identifier parentIdentifier = new Identifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM)
                                                      .setValue(NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_MEDICAL_INFORMATICS_INITIATIVE_CONSORTIUM);
        Coding memberOrganizationRole = new Coding().setSystem(CODESYSTEM_DSF_ORGANIZATION_ROLE).setCode(CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC);
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
        logger.info("-> {}", targets);
        variables.setTargets(variables.createTargets(targets));
    }
}
