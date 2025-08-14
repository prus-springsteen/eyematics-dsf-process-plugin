package org.eyematics.process.service.receive;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.RsaAesGcmUtil;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
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
    private final IParser parser;

    public DecryptRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, KeyProvider keyProvider) {
        super(api, dataSetStatusGenerator);
        this.keyProvider = keyProvider;
        FhirContext ctx = FhirContext.forR4();
        this.parser = ctx.newJsonParser();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.keyProvider, "keyProvider");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        try {
            logger.info("-> something - hopefully - to decrypt");
            String correlationKey = this.api.getVariables(delegateExecution).getTarget().getCorrelationKey();
            Binary bundleEncryptedBinary = (Binary) delegateExecution.getVariable(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED + correlationKey);
            String bundleString = new String(bundleEncryptedBinary.getData(), StandardCharsets.UTF_8);
            logger.info("Bundle -> {}", bundleString.substring(0, 100));
            String reqOrg = variables.getLatestTask().getRequester().getIdentifier().getValue();
            logger.info("Request-Organization -> {}", reqOrg);
            String recOrg = variables.getLatestTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
            logger.info("Recipient-Organization -> {}", recOrg);
            Bundle bundleDecrypted = this.decryptBundle(this.keyProvider.getPrivateKey(), bundleEncryptedBinary.getData(), reqOrg, recOrg);
            String o = this.parser.encodeResourceToString(bundleDecrypted);
            logger.info("Bundle -> {}", o.substring(0, 100));
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not decrypt downloaded data from DIC: {}", errorMessage);
            super.handleTaskError(EyeMaticsGenericStatus.DATA_DECRYPTION_FAILURE, variables, errorMessage);
        }
    }

    private Bundle decryptBundle(PrivateKey privateKey, byte[] bundleEncrypted, String sendingOrganization,
                                 String receivingOrganization) {
        try {
            byte[] bundleDecrypted = RsaAesGcmUtil.decrypt(privateKey, bundleEncrypted, sendingOrganization,
                    receivingOrganization);
            String bundleString = new String(bundleDecrypted, StandardCharsets.UTF_8);
            return (Bundle) FhirContext.forR4().newXmlParser().parseResource(bundleString);
        }
        catch (Exception exception) {
            logger.warn("Could not decrypt data-set - {}", exception.getMessage());
            throw new RuntimeException("Could not decrypt received data-set", exception);
        }
    }
}
