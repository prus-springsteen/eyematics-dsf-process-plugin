package org.eyematics.process.service.receive;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.RsaAesGcmUtil;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Objects;


public class DecryptRequestedDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DecryptRequestedDataTask.class);
    private final KeyProvider keyProvider;
    private final IParser parser;

    public DecryptRequestedDataTask(ProcessPluginApi api, KeyProvider keyProvider) {
        super(api);
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
        logger.info("-> something to decrypt");
        byte[] bundleEncrypted = variables.getByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED);
        String bundleString = new String(bundleEncrypted, StandardCharsets.UTF_8);
        logger.info("Bundle -> {}", bundleString.substring(0, 50));
        String reqOrg = variables.getLatestTask().getRequester().getIdentifier().getValue();
        logger.info("Request-Organization -> {}", reqOrg);
        String recOrg = variables.getLatestTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
        logger.info("Recipient-Organization -> {}", recOrg);
        Bundle bundleDecrypted = decryptBundle(this.keyProvider.getPrivateKey(), bundleEncrypted, reqOrg, recOrg);
        String o = this.parser.encodeResourceToString(bundleDecrypted);
        logger.info("Bundle -> {}", o.substring(0, 50));
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
            //throw new BpmnError(ConstantsDataTransfer.BPMN_EXECUTION_VARIABLE_DATA_RECEIVE_ERROR, error, exception);
        }
    }
}
