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
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
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


public class EncryptDataBundleTask extends AbstractExtendedProcessServiceDelegate {

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
        logger.info("-> Encrypting the local data for provision");
        String reqOrg = variables.getStartTask().getRequester().getIdentifier().getValue();
        String recOrg = variables.getStartTask().getRestriction().getRecipientFirstRep().getIdentifier().getValue();
        try {
            PublicKey pubKey = this.readPublicKey(reqOrg);
            Bundle bundle = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET);
            byte[] bundleEncrypted = this.encrypt(pubKey, bundle, recOrg, reqOrg);
            variables.setByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED, bundleEncrypted);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not encrypt data: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_ENCRYPT_FAILURE, variables, errorMessage);
        }
    }

    private PublicKey readPublicKey(String dicIdentifier) {
        String url = this.getEndpointUrl(dicIdentifier);
        Optional<Bundle> publicKeyBundleOptional = this.keyProvider.readPublicKeyIfExists(url);
        if (publicKeyBundleOptional.isEmpty())
            throw new IllegalStateException(
                    "Could not find PublicKey Bundle of organization with identifier'" + url + "'");
        logger.debug("Downloaded PublicKey Bundle from organization with identifier '{}'", url);
        Bundle publicKeyBundle = publicKeyBundleOptional.get();
        DocumentReference documentReference = this.getDocumentReference(publicKeyBundle);
        Binary binary = this.getBinary(publicKeyBundle);
        PublicKey publicKey = this.getPublicKey(binary, publicKeyBundle.getId());
        this.checkHash(documentReference, publicKey);
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
        } catch (Exception exception) {
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
        } catch (Exception exception) {
            logger.warn("Could not encrypt data-set to transmit - {}", exception.getMessage());
            throw new RuntimeException("Could not encrypt data-set to transmit - " + exception.getMessage());
        }
    }
}
