package org.eyematics.process.service.receive;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.RsaAesGcmUtil;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Objects;


public class DecryptRequestedDataTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DecryptRequestedDataTask.class);
    private final KeyProvider keyProvider;

    public DecryptRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, KeyProvider keyProvider) {
        super(api, dataSetStatusGenerator);
        this.keyProvider = keyProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.keyProvider, "keyProvider");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Decrypting the provided data");
        try {
            Binary bundleEncryptedBinary = (Binary) this.getVariable(delegateExecution,
                            ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED);
            String reqOrg = variables.getLatestTask().getRequester().getIdentifier().getValue();
            String recOrg = variables.getLatestTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
            Bundle bundleDecrypted = this.decryptBundle(this.keyProvider.getPrivateKey(),
                    bundleEncryptedBinary.getData(),
                    reqOrg,
                    recOrg);
            this.setVariable(delegateExecution,
                    ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET,
                    bundleDecrypted);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not decrypt downloaded data from DIC: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_DECRYPT_FAILURE, variables, errorMessage);
        }
    }

    private Bundle decryptBundle(PrivateKey privateKey, byte[] bundleEncrypted, String sendingOrganization,
                                 String receivingOrganization) {
        try {
            byte[] bundleDecrypted = RsaAesGcmUtil.decrypt(privateKey, bundleEncrypted, sendingOrganization,
                    receivingOrganization);
            String bundleString = new String(bundleDecrypted, StandardCharsets.UTF_8);
            return (Bundle) FhirContext.forR4().newXmlParser().parseResource(bundleString);
        } catch (Exception exception) {
            String errorMessage = "Could not decrypt data: " + exception.getMessage();
            logger.warn(errorMessage);
            throw new RuntimeException(errorMessage, exception);
        }
    }
}
