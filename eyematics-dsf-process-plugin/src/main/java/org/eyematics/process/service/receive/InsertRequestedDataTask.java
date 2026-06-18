package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Basic;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class InsertRequestedDataTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InsertRequestedDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public InsertRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator,
                                   FhirClientFactory fhirClientFactory) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Insertion of the provided data into FHIR repository.");

        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();

        Bundle referenceBundle = (Bundle) this.getVariable(delegateExecution,
                ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_REFERENCE_BUNDLE);

        AtomicReference<AtomicInteger> amountOfData = new AtomicReference<>(new AtomicInteger());
        AtomicReference<AtomicInteger> amountOfFailures = new AtomicReference<>(new AtomicInteger());
        AtomicReference<String> insertFailures = new AtomicReference<>("");

        referenceBundle.getEntry()
                .stream()
                .filter(e -> e.getResource().getResourceType().equals(ResourceType.Basic))
                .map(e -> (Basic) e.getResource())
                .forEach(b -> {
                    try {
                        String bundleId = b.getSubject().getReference();
                        Bundle bundle = (Bundle) this.getVariable(delegateExecution,
                                ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET, bundleId);

                        if (bundle == null) throw new Exception("Bundle is null");
                        if (bundle.getEntry().isEmpty()) throw new Exception("Bundle is empty");

                        bundle.getEntry()
                                .stream()
                                .filter(e -> e.getResource()
                                        .getResourceType().equals(ResourceType.Bundle))
                                .map(e -> (Bundle) e.getResource())
                                .filter(tb -> tb.getType().equals(Bundle.BundleType.TRANSACTION))
                                .forEach( tb -> {
                                    try {
                                        int sum = amountOfData.get().get() + this.insertBundle(tb, fhirClient);
                                        amountOfData.set(new AtomicInteger(sum));
                                        this.setVariable(delegateExecution, bundleId, null);
                                    } catch (Exception exception) {
                                        String tbId = tb.getIdElement().getIdPart();
                                        logger.error("Could not insert Transaction-Bundle ({}) to FHIR-Repository: {}",
                                                bundleId,
                                                exception.getMessage());
                                        amountOfFailures.set(new AtomicInteger(amountOfFailures.get()
                                                .get() + 1));
                                        insertFailures.set(insertFailures.get() + " " + exception.getMessage()
                                                + " (" + tbId + ")\n");
                                    }
                                });

                    } catch (Exception exception) {
                        String bundleId = b.getIdElement().getIdPart();
                        logger.error("Could not process Collection-Bundle ({}) for insertion to FHIR-Repository: {}",
                                bundleId,
                                exception.getMessage());
                        amountOfFailures.set(new AtomicInteger(amountOfFailures.get().get() + 1));
                        insertFailures.set(insertFailures.get() + " " + exception.getMessage()
                                + " (" + bundleId + ")\n");
                    }
                });

        if (amountOfData.get().get() == 0) {
            logger.error("Could not insert data to FHIR-Repository: {}",
                    insertFailures.get().replace("\n", "; "));
            this.handleTaskError(EyeMaticsGenericStatus.COMPLETE_INSERT_FAILURE, variables, insertFailures.get());
        }

        String providingOrganization = variables.getLatestTask().getRequester().getIdentifier().getValue();
        MailSender.sendInfo(this.api.getMailService(),
                variables.getLatestTask(),
                EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(),
                "Data inserted",
                String.format("%s inserted %d FHIR resources.", providingOrganization, amountOfData.get().get()));

        if (amountOfFailures.get().get() > 0) {
            String errorMessage = String.format("Could not insert all data, as %d attemps failed:%s. " +
                            "Only %d FHIR resources were inserted.",
                    amountOfFailures.get().get(),
                    insertFailures.get(),
                    amountOfData.get().get());
            this.handleTaskError(EyeMaticsGenericStatus.PARTIAL_INSERT_FAILURE, variables, errorMessage);
        }
    }

    private int insertBundle(Bundle bundle, EyeMaticsFhirClient fhirClient) throws Exception {
            String bundleString = this.api.getFhirContext().newJsonParser().encodeResourceToString(bundle);
            String methodOutcome = fhirClient.create(bundleString,
                    EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
            return this.countResources(methodOutcome);
    }

    private int countResources(String methodOutcome) {
        try {
            Bundle methodOutcomeBundle = (Bundle) this.api.getFhirContext()
                    .newJsonParser().parseResource(methodOutcome);
            return methodOutcomeBundle.getEntry().size();
        } catch (Exception exception) {
            return 0;
        }
    }
}
