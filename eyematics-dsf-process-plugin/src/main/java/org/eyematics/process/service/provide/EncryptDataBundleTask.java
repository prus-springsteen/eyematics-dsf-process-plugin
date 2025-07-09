/**
 * @author Reto Wettstein (https://github.com/wetret)
 * @see    https://github.com/medizininformatik-initiative/mii-process-data-transfer/blob/main/src/main/java/de/medizininformatik_initiative/process/data_transfer/service/EncryptData.java
 */

package org.eyematics.process.service.provide;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.codec.digest.DigestUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.constants.NamingSystems;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.RsaAesGcmUtil;
import org.apache.commons.codec.binary.Hex;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.generator.AbstractExtendedServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @see https://github.com/medizininformatik-initiative/mii-process-data-transfer/wiki/Process-Data-Transfer-Configuration-v1.0.x.x
 */

/**  openssl genrsa -out dic_keypair.pem 4096
 *  openssl rsa -in dic_keypair.pem -pubout -out dic_public_key.pem
 *  openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in dic_keypair.pem -out dic_private_key.pem
 *
 *  openssl genrsa -out cos_keypair.pem 4096
 *  openssl rsa -in cos_keypair.pem -pubout -out cos_public_key.pem
 *  openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in cos_keypair.pem -out cos_private_key.pem
 *
 *  openssl genrsa -out hrp_keypair.pem 4096
 *  openssl rsa -in hrp_keypair.pem -pubout -out hrp_public_key.pem
 *  openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in hrp_keypair.pem -out hrp_private_key.pem
 */

public class EncryptDataBundleTask extends AbstractExtendedServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(EncryptDataBundleTask.class);
    private final KeyProvider keyProvider;

    public EncryptDataBundleTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, KeyProvider keyProvider) {
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
        logger.info("-> try to get public-key ....");
        String reqOrg = variables.getStartTask().getRequester().getIdentifier().getValue();
        logger.info("EncryptProvideDataTask: Request-Organization -> {}", reqOrg);
        String recOrg = variables.getStartTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
        logger.info("EncryptProvideDataTask: Recipient-Organization -> {}", recOrg);
        try {
            PublicKey pubKey = this.readPublicKey(reqOrg);
            logger.info("-> something to encrypt");
            Bundle b = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET);
            byte[] bundleEncrypted = this.encrypt(pubKey, b, recOrg, reqOrg);
            variables.setByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED, bundleEncrypted);
        } catch (Exception exception) {
            logger.error("Could not encrypt data: {}", exception.getMessage());
            super.handleTaskError(EyeMaticsGenericStatus.DATA_ENCRYPT_FAILED, variables, exception, "Data Encrypt Failed");
        }
    }

    private PublicKey readPublicKey(String dmsIdentifier) {
        String url = getEndpointUrl(dmsIdentifier);
        Optional<Bundle> publicKeyBundleOptional = this.keyProvider.readPublicKeyIfExists(url);
        if (publicKeyBundleOptional.isEmpty())
            throw new IllegalStateException(
                    "Could not find PublicKey Bundle of organization with identifier'" + url + "'");
        logger.debug("Downloaded PublicKey Bundle from organization with identifier '{}'", url);
        Bundle publicKeyBundle = publicKeyBundleOptional.get();
        DocumentReference documentReference = getDocumentReference(publicKeyBundle);
        Binary binary = getBinary(publicKeyBundle);
        PublicKey publicKey = getPublicKey(binary, publicKeyBundle.getId());
        checkHash(documentReference, publicKey);
        return publicKey;
    }

    private String getEndpointUrl(String identifier) {
        return api.getEndpointProvider().getEndpointAddress(NamingSystems.OrganizationIdentifier.withValue(
                                EyeMaticsConstants.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_EYEMATICS),
                        NamingSystems.OrganizationIdentifier.withValue(identifier),
                        new Coding().setSystem(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE)
                                .setCode(EyeMaticsConstants.CODESYSTEM_DSF_ORGANIZATION_ROLE_VALUE_DIC))
                .orElseThrow(() -> new RuntimeException(
                        "Could not find Endpoint for organization with identifier '" + identifier + "'"));
    }

    private DocumentReference getDocumentReference(Bundle bundle) {
        List<DocumentReference> documentReferences = bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource).filter(r -> r instanceof DocumentReference)
                .map(r -> (DocumentReference) r).toList();
        if (documentReferences.isEmpty())
            throw new IllegalArgumentException("Could not find any DocumentReference in PublicKey Bundle");
        if (documentReferences.size() > 1)
            logger.warn("Found {} DocumentReferences in PublicKey Bundle, using the first", documentReferences.size());
        return documentReferences.get(0);
    }

    private Binary getBinary(Bundle bundle) {
        List<Binary> binaries = bundle.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Binary).map(b -> (Binary) b).toList();
        if (binaries.isEmpty())
            throw new IllegalArgumentException("Could not find any Binary in PublicKey Bundle");
        if (binaries.size() > 1)
            logger.warn("Found {} Binaries in PublicKey Bundle, using the first", binaries.size());
        return binaries.get(0);
    }

    private PublicKey getPublicKey(Binary binary, String publicKeyBundleId) {
        try {
            return KeyProvider.fromBytes(binary.getContent());
        }
        catch (Exception exception) {
            logger.warn("Could not read PublicKey from Binary in PublicKey Bundle with id '{}' - {}", publicKeyBundleId,
                    exception.getMessage());
            throw new RuntimeException("Could not read PublicKey from Binary in PublicKey Bundle with id '"
                    + publicKeyBundleId + "' - " + exception.getMessage(), exception);
        }
    }

    private void checkHash(DocumentReference documentReference, PublicKey publicKey) {
        long numberOfHashes = documentReference.getContent().stream()
                .filter(DocumentReference.DocumentReferenceContentComponent::hasAttachment)
                .map(DocumentReference.DocumentReferenceContentComponent::getAttachment).filter(Attachment::hasHash)
                .map(Attachment::getHash).count();
        if (numberOfHashes < 1)
            throw new RuntimeException("Could not find any sha256-hash in DocumentReference");
        if (numberOfHashes > 1)
            logger.warn("DocumentReference contains {} sha256-hashes, using the first", numberOfHashes);
        byte[] documentReferenceHash = documentReference.getContentFirstRep().getAttachment().getHash();
        byte[] publicKeyHash = DigestUtils.sha256(publicKey.getEncoded());
        logger.debug("DocumentReference PublicKey sha256-hash '{}'", Hex.encodeHexString(documentReferenceHash));
        logger.debug("PublicKey actual sha256-hash '{}'", Hex.encodeHexString(publicKeyHash));
        if (!Arrays.equals(documentReferenceHash, publicKeyHash))
            throw new RuntimeException(
                    "Sha256-hash in DocumentReference does not match computed sha256-hash of Binary");
    }

    private byte[] encrypt(PublicKey publicKey, Bundle bundle, String sendingOrganizationIdentifier,
                           String receivingOrganizationIdentifier) {
        try {
            byte[] toEncrypt = FhirContext.forR4().newXmlParser().encodeResourceToString(bundle)
                    .getBytes(StandardCharsets.UTF_8);
            return RsaAesGcmUtil.encrypt(publicKey, toEncrypt, sendingOrganizationIdentifier,
                    receivingOrganizationIdentifier);
        }
        catch (Exception exception) {
            logger.warn("Could not encrypt data-set to transmit - {}", exception.getMessage());
            throw new RuntimeException("Could not encrypt data-set to transmit - " + exception.getMessage());
        }
    }
}
