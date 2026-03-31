package org.eyematics.process.service.provide;

import java.util.Objects;
import java.util.stream.Collectors;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.bpe.EyeMaticsDataBundleRetriever;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.*;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadMedicinalDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadMedicinalDataTask.class);
    private final FhirClientFactory fhirClientFactory;
    private final int fhirStoreResourcePageSize;

    public ReadMedicinalDataTask(ProcessPluginApi api,
                                 DataSetStatusGenerator dataSetStatusGenerator,
                                 FhirClientFactory fhirClientFactory,
                                 int fhirStoreResourcePageSize) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
        this.fhirStoreResourcePageSize = fhirStoreResourcePageSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Reading local data from FHIR repository is initiated");
        try {
            EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
            MailSender.sendInfo(this.api.getMailService(),
                    variables.getStartTask(),
                    "-",
                    "Data requested",
                    "there is a new MDAT request which is processed.");

            String observationQuery = String.format("_profile=%s&_count=%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_OBSERVATION_PROFILE.stream()
                    .map(s -> EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI + s)
                    .collect(Collectors.joining(",")),
                    this.fhirStoreResourcePageSize);
            Bundle observations = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Observation", observationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                    observations);
            String medicationQuery = String.format("_profile=%s%s&_count=%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_PROFILE,
                    this.fhirStoreResourcePageSize);
            Bundle medications = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Medication", medicationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET,
                    medications);
            String medicationAdministrationQuery = String.format("_profile=%s%s&_count=%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_ADMINISTRATION_PROFILE,
                    this.fhirStoreResourcePageSize);
            Bundle medicationAdministrations = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "MedicationAdministration", medicationAdministrationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET,
                    medicationAdministrations);
            String medicationRequestQuery = String.format("_profile=%s%s&_count=%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_REQUEST_PROFILE,
                    this.fhirStoreResourcePageSize);
            Bundle medicationRequests = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "MedicationRequest", medicationRequestQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET,
                    medicationRequests);

        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read EyeMatics data from FHIR repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_READ_FAILURE, variables, errorMessage);
        }
    }
}
