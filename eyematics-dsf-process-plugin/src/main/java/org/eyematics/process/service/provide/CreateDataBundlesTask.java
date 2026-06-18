package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class CreateDataBundlesTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateDataBundlesTask.class);
    private final int maximumPatientPerBundle;

    public CreateDataBundlesTask(ProcessPluginApi api,
                                 DataSetStatusGenerator dataSetStatusGenerator,
                                 int maximumPatientPerBundle) {
        super(api, dataSetStatusGenerator);
        this.maximumPatientPerBundle = maximumPatientPerBundle;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Bundling the local data for provision.");
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
            Bundle globalPseudonymBundle = new Bundle().setType(Bundle.BundleType.COLLECTION);
            Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
            brc.setMethod(Bundle.HTTPVerb.PUT);

            Bundle commonDataBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
            commonDataBundle.setId(UUID.randomUUID().toString());
            Organization organization =
                    variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_ORGANIZATION_DATA_SET);
            commonDataBundle.addEntry()
                    .setResource(organization)
                    .setRequest(brc.copy().setUrl(this.toFullURL(organization)))
                    .setFullUrl(this.toFullURL(organization));
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_ORGANIZATION_DATA_SET, null);

            List<Bundle.BundleEntryComponent> measureReportEntries = this.processAndGetCommonBundle(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEASURE_REPORT_DATA_SET, brc);
            List<Bundle.BundleEntryComponent> medicationEntries = this.processAndGetCommonBundle(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET, brc);

            commonDataBundle.getEntry().addAll(measureReportEntries);
            commonDataBundle.getEntry().addAll(medicationEntries);

            AtomicBoolean isCommonAdded = new AtomicBoolean(false);

            AtomicReference<Bundle> particularBundle =
                    new AtomicReference<>(new Bundle().setType(Bundle.BundleType.TRANSACTION));
            particularBundle.get().setId(UUID.randomUUID().toString());
            AtomicReference<String> particularBundleId = new AtomicReference<>(particularBundle.get()
                    .getIdElement().getIdPart());
            AtomicReference<Bundle> referenceBundle =
                    new AtomicReference<>(new Bundle().setType(Bundle.BundleType.COLLECTION));
            referenceBundle.get().setId(UUID.randomUUID().toString());
            referenceBundle.get().addEntry().setResource(particularBundle.get().copy());

            AtomicInteger bundleCount = new AtomicInteger();

            patients.getEntry()
                    .stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(be -> be.getResourceType().equals(ResourceType.Patient))
                    .map(be -> (Patient) be)
                    .forEach(p -> {

                        if (!isCommonAdded.get()) {
                            isCommonAdded.set(true);
                            particularBundle.get().addEntry().setResource(commonDataBundle);
                            bundleCount.getAndIncrement();
                        }

                        if (bundleCount.get() >= this.maximumPatientPerBundle) {
                            this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET,
                                    particularBundleId.get(), particularBundle.get().copy());
                            bundleCount.set(0);
                            particularBundle.set(new Bundle().setType(Bundle.BundleType.COLLECTION));
                            particularBundle.get().setId(UUID.randomUUID().toString());
                            particularBundleId.set(particularBundle.get().getIdElement().getIdPart());
                            referenceBundle.get().addEntry().setResource(particularBundle.get().copy());
                        }

                        Bundle patientBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
                        patientBundle.setId(UUID.randomUUID().toString());
                        patientBundle.addEntry()
                                .setResource(p)
                                .setRequest(brc.copy().setUrl(this.toFullURL(p)))
                                .setFullUrl(p.getIdElement().toUnqualifiedVersionless().getValue());

                        String patientId = p.getIdElement().getValue();

                        List<Bundle.BundleEntryComponent> observationEntries =
                                this.processAndGetPatientAssociatedBundle(variables, patientId,
                                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET, brc);
                        List<Bundle.BundleEntryComponent> diagnosticReportEntries =
                                this.processAndGetPatientAssociatedBundle(variables, patientId,
                                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET, brc);
                        List<Bundle.BundleEntryComponent> medicationAdministrationEntries =
                                this.processAndGetPatientAssociatedBundle(variables, patientId,
                                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, brc);
                        List<Bundle.BundleEntryComponent> medicationRequestEntries =
                                this.processAndGetPatientAssociatedBundle(variables, patientId,
                                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, brc);

                        patientBundle.getEntry().addAll(observationEntries);
                        patientBundle.getEntry().addAll(diagnosticReportEntries);
                        patientBundle.getEntry().addAll(medicationAdministrationEntries);
                        patientBundle.getEntry().addAll(medicationRequestEntries);

                        particularBundle.get().addEntry().setResource(patientBundle);
                        Patient globalPseudonymPatient = new Patient().addIdentifier(p.getIdentifier().get(0));
                        globalPseudonymPatient.setId(patientId);
                        globalPseudonymBundle.addEntry().setResource(globalPseudonymPatient);

                        bundleCount.getAndIncrement();
                    });

            this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET,
                    particularBundleId.get(), particularBundle.get().copy());

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE_BUNDLE,
                    referenceBundle.get());

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_GLOBAL_PSEUDONYMS,
                    globalPseudonymBundle);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not bundle data: {}.", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_BUNDLE_FAILURE, variables, errorMessage);
        }
    }

    private String toFullURL(Resource resource) {
        return resource.getResourceType().toString() + "/" + resource.getIdElement().getIdPart();
    }

    private List<Bundle.BundleEntryComponent> processAndGetCommonBundle(Variables variables,
                                                                 String resourceName,
                                                                 Bundle.BundleEntryRequestComponent brc) {
        Bundle bundle = variables.getResource(resourceName);
        if (bundle != null) {
            List<Bundle.BundleEntryComponent> bundleEntries = bundle.getEntry()
                    .stream()
                    .parallel()
                    .filter(Bundle.BundleEntryComponent::hasResource)
                    .map(Bundle.BundleEntryComponent::getResource)
                    .map(o ->
                            new Bundle.BundleEntryComponent().setResource(o)
                                    .setRequest(brc.copy().setUrl(this.toFullURL(o)))
                                    .setFullUrl(this.toFullURL(o))
                    )
                    .toList();
            variables.setResource(resourceName, null);
            return bundleEntries;
        }
        return List.of();
    }

    private List<Bundle.BundleEntryComponent> processAndGetPatientAssociatedBundle(Variables variables,
                                                                                   String patientId,
                                                                                   String resourceName,
                                                                                   Bundle.BundleEntryRequestComponent brc) {
        Bundle bundle = this.getResource(variables, resourceName, patientId);
        if (bundle != null) {
            List<Bundle.BundleEntryComponent> bundleEntries = bundle.getEntry()
                    .stream()
                    .parallel()
                    .filter(Bundle.BundleEntryComponent::hasResource)
                    .map(Bundle.BundleEntryComponent::getResource)
                    .map(o ->
                        new Bundle.BundleEntryComponent().setResource(o)
                                .setRequest(brc.copy().setUrl(this.toFullURL(o)))
                                .setFullUrl(this.toFullURL(o))
                    )
                    .toList();
            this.setResource(variables, resourceName, patientId, null);
            return bundleEntries;
        }
        return List.of();
    }
}
