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


public class DecryptDataReceiveTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DecryptDataReceiveTask.class);
    private final KeyProvider keyProvider;
    private final IParser parser;

    public DecryptDataReceiveTask(ProcessPluginApi api, KeyProvider keyProvider) {
        super(api);
        this.keyProvider = keyProvider;
        FhirContext ctx = FhirContext.forR4();
        this.parser = ctx.newJsonParser();
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("DecryptDataReceiveTask -> something to decrypt");
        byte[] bundleEncrypted = variables.getByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED);
        String bundleString = new String(bundleEncrypted, StandardCharsets.UTF_8);
        logger.info("EncryptProvideDataTask: Bundle -> {}", bundleString);
        String reqOrg = variables.getLatestTask().getRequester().getIdentifier().getValue();
        logger.info("EncryptProvideDataTask: Request-Organization -> {}", reqOrg);
        String recOrg = variables.getLatestTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
        logger.info("EncryptProvideDataTask: Recipient-Organization -> {}", recOrg);
        Bundle bundleDecrypted = decryptBundle(keyProvider.getPrivateKey(), bundleEncrypted, reqOrg, recOrg);
        String o = this.parser.encodeResourceToString(bundleDecrypted);
        logger.info("EncryptProvideDataTask: Bundle -> {}", o);
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
